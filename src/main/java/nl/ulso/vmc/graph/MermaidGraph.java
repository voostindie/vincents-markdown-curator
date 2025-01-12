package nl.ulso.vmc.graph;

import nl.ulso.markdown_curator.DataModelTemplate;
import nl.ulso.markdown_curator.journal.Journal;
import nl.ulso.markdown_curator.journal.MarkedLine;
import nl.ulso.markdown_curator.vault.*;
import nl.ulso.markdown_curator.vault.event.*;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.LocalDate;
import java.util.*;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static nl.ulso.markdown_curator.vault.InternalLinkFinder.parseInternalLinkTargetNames;
import static nl.ulso.markdown_curator.vault.LocalDates.parseDateOrNull;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Builds a graph from notes in the repository.
 * <p/>
 * This graph is <strong>not</strong> built from all notes and the links between them. There are
 * several reasons for that, all relating to the fact that this curator uses queries to generate
 * new output within notes:
 * <p/>
 * <ul>
 *     <li>Without query output, there often aren't many links, or any links at all. A note can be
 *     built up from queries only. The curator looks at page content <strong>without</strong> query
 *     output. So, there are few links in the content, and therefore few relations to discover.</li>
 *     <li>With query output, there are often too many links. They're also not stable. Take the
 *     {@code timeline} query as an example. Because it generates the context for each item in
 *     the journal, it might include much more than is needed. Also, the query can be configured to
 *     have limit the number of results. If the limit is reached, every new entry pushes out the
 *     oldest one. That makes the links unstable.
 *     </li>
 * </ul>
 * <p/>
 * So, how is the graph generated? First off, documents are included only in the graph as nodes
 * if there is an associated {@link Type} for it, linked to a folder in the repository. Secondly,
 * edges between nodes are constructed by pulling them out of the journal, from marked lines, as
 * supported by the {@link Journal}. Only markers that have set the front matter property
 * {@code include-in-graph} are considered.
 * <p/>
 * Nodes that are in a subfolder of the type folder are considered to be archived. They are
 * rendered differently.
 */
@Singleton
public class MermaidGraph
        extends DataModelTemplate
{
    private static final Logger LOGGER = getLogger(MermaidGraph.class);

    private static final String MARKER_PROPERTY_INCLUDE_IN_GRAPH = "include-in-graph";

    private final Vault vault;
    private final Journal journal;
    private final MermaidGraphSettings settings;
    private final Set<String> selectedMarkerNames;
    private final Map<String, Node> nodes;

    @Inject
    MermaidGraph(Vault vault, Journal journal, MermaidGraphSettings settings)
    {
        this.vault = vault;
        this.journal = journal;
        this.settings = settings;
        this.selectedMarkerNames = new HashSet<>();
        this.nodes = new HashMap<>();
    }

    public int order()
    {
        return journal.order() + 1;
    }

    @Override
    public void fullRefresh()
    {
        refreshSelectedMarkers();
        refreshNodes();
        refreshEdges();
        LOGGER.debug("Constructed a graph with {} nodes", nodes.size());
    }

    @Override
    public void process(DocumentAdded event)
    {
        processDocumentUpdate(event.document());
    }

    @Override
    public void process(DocumentChanged event)
    {
        processDocumentUpdate(event.document());
    }

    @Override
    public void process(DocumentRemoved event)
    {
        processDocumentUpdate(event.document());
    }

    private void processDocumentUpdate(Document document)
    {
        if (journal.isMarkerDocument(document))
        {
            // A marker may have been added or removed, or an existing one updated. Both can affect
            // the `include-in-graph` setting, and therefore the graph as a whole. A full refresh
            // is the easiest thing to do. Also, this doesn't happen much. Markers are stable.
            fullRefresh();
        }
        else if (journal.isJournalEntry(document))
        {
            var date = parseDateOrNull(document.name());
            if (date != null)
            {
                // The document refers to a daily log. That means all node edges for that day need
                // to be refreshed.
                refreshGraphForJournalEntryOn(date);
            }
        }
        else if (isNodeEntry(document)) // Document represents a node
        {
            var name = document.name();
            var node = nodes.get(name);
            if (node != null)
            {
                // A node already exists. All we need to do is replace the underlying document
                // reference.
                node.replaceDocumentWith(document);
            }
            else
            {
                // This is a new node. It might refer to other nodes already, and other nodes
                // might refer to it. So, a full refresh is all we can do.
                fullRefresh();
            }
        }
    }

    private boolean isNodeEntry(Document document)
    {
        var folder = document.folder();
        while (folder != vault)
        {
            if (settings.folderNames().contains(folder.name()))
            {
                return true;
            }
            folder = folder.parent();
        }
        return false;
    }

    private void refreshGraphForJournalEntryOn(LocalDate date)
    {
        // First remove all edges from the graph for the given date
        nodes.values().forEach(node -> node.removeEdgesFor(date));
        // Then add them back in. These might be completely different!
        nodes.values().forEach(node -> processMarkedLines(
                journal.markedLinesFor(node.document().name(), selectedMarkerNames, date),
                node));
    }

    private void refreshSelectedMarkers()
    {
        selectedMarkerNames.clear();
        selectedMarkerNames.addAll(
                journal.markers().values().stream()
                        .filter(marker ->
                                marker.frontMatter().bool(MARKER_PROPERTY_INCLUDE_IN_GRAPH, false))
                        .map(Document::name)
                        .collect(toSet())
        );
    }

    private void refreshNodes()
    {
        nodes.clear();
        for (Type type : settings.nodeTypes())
        {
            var nodeFinder = new NodeFinder(type);
            vault.folder(type.folderName()).ifPresent(folder -> folder.accept(nodeFinder));
        }
    }

    private void refreshEdges()
    {
        for (Node sourceNode : nodes.values())
        {
            processMarkedLines(
                    journal.markedLinesFor(sourceNode.document().name(), selectedMarkerNames),
                    sourceNode);
        }
    }

    private void processMarkedLines(Map<String, List<MarkedLine>> markedLines, Node sourceNode)
    {
        markedLines
                .values().stream()
                .flatMap(Collection::stream)
                .forEach(markedLine ->
                {
                    var targetNames = parseInternalLinkTargetNames(markedLine.line());
                    for (String targetName : targetNames)
                    {
                        var targetNode = nodes.get(targetName);
                        if (targetNode == null)
                        {
                            continue;
                        }
                        var date = markedLine.date();
                        sourceNode.addEdge(targetNode, date);
                        targetNode.addEdge(sourceNode, date);
                    }
                });
    }

    public String mermaidGraphFor(
            String seedDocumentName, int maximumDepth, Set<String> excludedTypeNames)
    {
        var seedNode = nodes.get(seedDocumentName);
        if (seedNode == null)
        {
            return "";
        }
        var excludedTypes = settings.nodeTypes().stream()
                .filter(t -> excludedTypeNames.contains(t.typeName()))
                .collect(toSet());
        return mermaidGraphFor(Set.of(seedNode), maximumDepth, excludedTypes);
    }

    private String mermaidGraphFor(
            Set<Node> selection, int maximumDepth, Set<Type> excludedTypes)
    {
        var builder = new StringBuilder();
        builder.append("```mermaid\n");
        builder.append("graph LR\n");
        renderClassDefinitions(builder);
        var visited = renderNodesAndEdges(builder, selection, maximumDepth, excludedTypes);
        renderNodesAsInternalLinks(builder, visited);
        builder.append("```\n");
        return builder.toString();
    }

    /**
     * For each class introduced by each classifier, generate the proper Mermaid code.
     *
     * @param builder builder to write the output to.
     */
    private void renderClassDefinitions(StringBuilder builder)
    {
        for (Map.Entry<String, String> entry : settings.mermaidClassDefinitions().entrySet())
        {
            builder.append("    classDef ")
                    .append(entry.getKey())
                    .append(" ")
                    .append(entry.getValue())
                    .append("\n");
        }
        builder.append("\n");
    }

    /**
     * Starting with a specific set of node, render all related nodes and edges up to a certain
     * depth.
     *
     * @param builder       builder to write the output to.
     * @param selection     nodes to start with.
     * @param maximumDepth  maximum depth to go, as counted from the seed node.
     * @param excludedTypes set of types to exclude from the graph
     * @return Node names that were rendered.
     */
    private HashSet<Node> renderNodesAndEdges(
            StringBuilder builder, Set<Node> selection, int maximumDepth,
            Set<Type> excludedTypes)
    {
        var queue = new LinkedList<Item>();
        selection.forEach(node -> queue.add(new Item(node, 1)));
        var visited = new HashSet<Node>();
        while (!queue.isEmpty())
        {
            var item = queue.removeFirst();
            var sourceNode = item.node();
            if (visited.contains(sourceNode))
            {
                continue;
            }
            builder.append("    ").append(sourceNode.toMermaidNode()).append("\n");
            visited.add(sourceNode);
            var depth = item.depth();
            if (depth > maximumDepth)
            {
                continue;
            }
            for (Node targetNode : sourceNode.edges())
            {
                if (excludedTypes.contains(targetNode.nodeType()))
                {
                    continue;
                }
                if (!visited.contains(targetNode))
                {
                    queue.addLast(new Item(targetNode, depth + 1));
                    builder.append("    ")
                            .append(sourceNode.id())
                            .append(" --- ")
                            .append(targetNode.id())
                            .append("\n");
                }
            }
        }
        return visited;
    }

    /**
     * Add the {@code internal-link} classification to each node, so that they turn into actual
     * links in Obsidian.
     *
     * @param builder builder to write the output to.
     * @param visited set of nodes that was generated in the graph.
     */
    private void renderNodesAsInternalLinks(StringBuilder builder, HashSet<Node> visited)
    {
        builder.append("    class ")
                .append(visited.stream()
                        .map(Node::id)
                        .sorted()
                        .collect(joining(",")))
                .append(" internal-link\n");
    }

    private class NodeFinder
            extends BreadthFirstVaultVisitor
    {
        private final Type type;
        private boolean isArchived;

        public NodeFinder(Type type)
        {
            this.type = type;
        }

        @Override
        public void visit(Folder folder)
        {
            isArchived = !folder.name().contentEquals(type.folderName());
            super.visit(folder);
        }

        @Override
        public void visit(Document document)
        {
            nodes.put(document.name(), new Node(document, type, isArchived));
        }
    }

    private record Item(Node node, int depth) {}
}
