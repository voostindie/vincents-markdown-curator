package nl.ulso.vmc.graph;

import nl.ulso.markdown_curator.vault.Document;

import java.time.LocalDate;
import java.util.*;

import static nl.ulso.hash.ShortHasher.shortHashOf;

/**
 * Represents a single node in the graph.
 * <p/>
 * Edges are bidirectional. The same edge is registered on both nodes involved. That means that if
 * an edge is added or removed to the graph, this must be done to both nodes, always. The reason to
 * do this is to be able to efficiently generate graphs, starting from any seed node.
 */
public final class Node
        implements Comparable<Node>
{
    private final String id;
    private final Type type;
    private final boolean isArchived;
    private final Map<Node, EdgeData> edges;
    private Document document;

    Node(Document document, Type type, boolean isArchived)
    {
        this.id = shortHashOf(document.name());
        this.type = type;
        this.isArchived = isArchived;
        this.edges = new TreeMap<>();
        this.document = document;
    }

    public String id()
    {
        return id;
    }

    public Document document() {return document;}

    public Type nodeType() {return type;}

    public boolean isArchived() {return isArchived;}

    public Set<Node> edges()
    {
        return Collections.unmodifiableSet(edges.keySet());
    }

    public String toMermaidNode()
    {
        var className = type.classifier().classify(this);
        return type.shape().toMermaidNode(id, document().name(), className);
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        Node node = (Node) o;
        return document.equals(node.document);
    }

    @Override
    public int hashCode()
    {
        return document.hashCode();
    }

    @Override
    public int compareTo(Node node)
    {
        return document.sortableTitle().compareTo(node.document.sortableTitle());
    }

    void addEdge(Node targetNode, LocalDate date)
    {
        if (this == targetNode)
        {
            return;
        }
        edges.computeIfAbsent(targetNode, n -> new EdgeData()).addDate(date);
    }

    void removeEdgesFor(LocalDate date)
    {
        var edgesToRemove = new HashSet<Node>();
        edges.forEach((key, edge) -> {
            edge.removeDate(date);
            if (edge.isInvalid())
            {
                edgesToRemove.add(key);
            }
        });
        edgesToRemove.forEach(edges::remove);
    }
    
    void replaceDocumentWith(Document newDocument)
    {
        if (!document.name().equals(newDocument.name()))
        {
            throw new IllegalStateException(
                    "Document name mismatch. Only replace documents if they represent the same " +
                    "file!");
        }
        this.document = newDocument;
    }
}
