package nl.ulso.vmc.rabobank;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.markdown_curator.project.Attribute;
import nl.ulso.markdown_curator.project.ProjectRepository;
import nl.ulso.vmc.graph.DefaultNodeClassifier;
import nl.ulso.vmc.graph.Node;

import java.util.*;

@Singleton
public class ProjectNodeClassifier
        extends DefaultNodeClassifier
{
    private final ProjectRepository repository;

    @Inject
    ProjectNodeClassifier(ProjectRepository projectRepository)
    {
        repository = projectRepository;
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
            var project = repository.projectsByName().get(node.document().name());
            if (project == null)
            {
                return Optional.of(toMermaid("unknown"));
            }
            return project.attributeValue(Attribute.STATUS).map(this::toMermaid);

        });
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
