package nl.ulso.vmc.graph;

import java.util.*;

public interface MermaidNodeClassifier
{
    MermaidNodeClassifier DEFAULT_CLASSIFIER = new DefaultNodeClassifier();

    default Map<String, String> classDefinitions()
    {
        return Collections.emptyMap();
    }

    default Optional<String> classify(Node node)
    {
        return Optional.empty();
    }
}
