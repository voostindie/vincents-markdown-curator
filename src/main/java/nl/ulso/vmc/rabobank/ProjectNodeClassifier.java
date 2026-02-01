package nl.ulso.vmc.rabobank;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.addon.project.AttributeRegistry;
import nl.ulso.curator.addon.project.Project;
import nl.ulso.curator.vault.Document;
import nl.ulso.vmc.graph.DefaultNodeClassifier;
import nl.ulso.vmc.graph.Node;

import java.util.*;

import static nl.ulso.curator.addon.project.AttributeDefinition.STATUS;

@Singleton
public class ProjectNodeClassifier
    extends DefaultNodeClassifier
{
    private final AttributeRegistry attributeRegistry;

    @Inject
    ProjectNodeClassifier(AttributeRegistry attributeRegistry)
    {
        this.attributeRegistry = attributeRegistry;
    }

    @Override
    public Map<String, String> classDefinitions()
    {
        var map = new HashMap<>(super.classDefinitions());
        map.put("green", "stroke:green");
        map.put("amber", "stroke:orange");
        map.put("red", "stroke:red");
        map.put("paused", "stroke:gray");
        map.put("unknown", "stroke:purple");
        map.put("done", "stroke:white");
        map.put("trashed", "stroke:yellow");
        return Collections.unmodifiableMap(map);
    }

    @Override
    public Optional<String> classify(Node node)
    {
        return super.classify(node).or(() -> {
            var project = projectFor(node.document(), attributeRegistry);
            return project
                .flatMap(p -> attributeRegistry.attributeValue(p, STATUS))
                .map(s -> (String) s)
                .map(this::toMermaid)
                .or(() -> Optional.of(toMermaid("unknown")));
        });
    }

    private Optional<Project> projectFor(Document document, AttributeRegistry registry)
    {
        return registry.projects().stream()
            .filter(project -> project.document().name().equals(document.name()))
            .findFirst();
    }

    private String toMermaid(String status)
    {
        return switch (status)
        {
            case "🟢" -> "green";
            case "🟠" -> "amber";
            case "🔴" -> "red";
            case "✅" -> "done";
            case "🗑️" -> "trashed";
            case "⛔️" -> "paused";
            default -> "unknown";
        };
    }

}
