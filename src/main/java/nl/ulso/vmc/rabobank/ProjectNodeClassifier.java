package nl.ulso.vmc.rabobank;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.markdown_curator.project.ProjectPropertyRepository;
import nl.ulso.vmc.graph.DefaultNodeClassifier;
import nl.ulso.vmc.graph.Node;

import java.util.*;

import static nl.ulso.markdown_curator.project.ProjectProperty.STATUS;

@Singleton
public class ProjectNodeClassifier
        extends DefaultNodeClassifier
{
    private final ProjectPropertyRepository projectPropertyRepository;

    @Inject
    ProjectNodeClassifier(ProjectPropertyRepository projectPropertyRepository)
    {
        this.projectPropertyRepository = projectPropertyRepository;
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
            var project = projectPropertyRepository.projectFor(node.document());
            return project
                    .flatMap(p -> projectPropertyRepository.propertyValue(p, STATUS))
                    .map(s -> (String) s)
                    .map(this::toMermaid)
                    .or(() -> Optional.of(toMermaid("unknown")));
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
