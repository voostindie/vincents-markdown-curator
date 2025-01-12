package nl.ulso.vmc.graph;

import nl.ulso.markdown_curator.query.*;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Map;

import static java.lang.Integer.max;

/**
 * Query to generate graphs from notes.
 * <p/>
 * See {@link MermaidGraph} for details on how the graph is constructed.
 */
public final class MermaidGraphQuery
        implements Query
{
    private final MermaidGraph graph;
    private final QueryResultFactory resultFactory;

    @Inject
    public MermaidGraphQuery(MermaidGraph mermaidGraph, QueryResultFactory resultFactory)
    {
        this.graph = mermaidGraph;
        this.resultFactory = resultFactory;
    }

    @Override
    public String name()
    {
        return "graph";
    }

    @Override
    public String description()
    {
        return "outputs a Mermaid graph";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return Map.of(
                "seed", "Document to start with; defaults to the current document",
                "depth", "Number of edges to follow from the source; defaults to 1",
                "exclude", "List of node types to exclude from the graph; defaults to empty."
        );
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        var seedName = definition.configuration().string("seed", definition.document().name());
        var maxDepth = max(definition.configuration().integer("depth", 1), 0);
        var excludedTypeNames = new HashSet<>(definition.configuration().listOfStrings("exclude"));
        var mermaid = graph.mermaidGraphFor(seedName, maxDepth, new HashSet<>(excludedTypeNames));
        if (mermaid.isBlank())
        {
            return resultFactory.error("No graph available.");
        }
        return resultFactory.string(mermaid);
    }
}
