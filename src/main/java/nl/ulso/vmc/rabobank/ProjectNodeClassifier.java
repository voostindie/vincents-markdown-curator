package nl.ulso.vmc.rabobank;

import nl.ulso.vmc.graph.DefaultNodeClassifier;
import nl.ulso.vmc.graph.Node;

import java.util.*;

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
        return super.classify(node).or(() -> {
            var status = node.document().frontMatter().string("status", "unknown");
            return Optional.of(toMermaid(status));

        });
    }

    private String toMermaid(String status)
    {
        return switch (status) {
            case "🟢" -> "green";
            case "🟠" -> "amber";
            case "🔴" -> "red";
            case "⭕️" -> "on-hold";
            default -> "unknown";
        };
    }


}
