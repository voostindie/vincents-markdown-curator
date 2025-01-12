package nl.ulso.vmc.project;

import nl.ulso.vmc.graph.DefaultNodeClassifier;
import nl.ulso.vmc.graph.Node;

import java.util.*;

import static nl.ulso.vmc.project.Status.fromString;

public class ProjectNodeClassifier
        extends DefaultNodeClassifier
{
    @Override
    public Map<String, String> classDefinitions()
    {
        var map = new HashMap<>(super.classDefinitions());
        map.put("green", "stroke:green");
        map.put("amber", "stroke:orange");
        map.put("red", "stroke:red");
        map.put("unknown", "stroke:purple");
        return Collections.unmodifiableMap(map);
    }

    @Override
    public Optional<String> classify(Node node)
    {
        return super.classify(node).or(() ->
                Optional.of(fromString(
                        node.document()
                                .frontMatter()
                                .string("status", "unknown"))
                        .toMermaid()));
    }
}
