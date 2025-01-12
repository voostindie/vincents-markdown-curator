package nl.ulso.vmc.graph;

import java.util.Map;
import java.util.Optional;

public class DefaultNodeClassifier
        implements MermaidNodeClassifier
{
    @Override
    public Map<String, String> classDefinitions()
    {
        return Map.of("archived", "stroke:gray");
    }

    @Override
    public Optional<String> classify(Node node)
    {
        if (node.isArchived())
        {
            return Optional.of("archived");
        }
        return Optional.empty();
    }
}
