package nl.ulso.vmc.omnifocus;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.markdown_curator.project.*;

import java.util.Map;
import java.util.Optional;

@Singleton
public class OmniFocusUrlResolver
        implements ProjectPropertyResolver
{
    public static final String OMNIFOCUS_URL = "omnifocus";
    private final ProjectProperty property;
    private final OmniFocusRepository repository;

    @Inject
    public OmniFocusUrlResolver(
            Map<String, ProjectProperty> properties, OmniFocusRepository repository)
    {
        this.property = properties.get(OMNIFOCUS_URL);
        this.repository = repository;
    }

    @Override
    public int order()
    {
        return 0;
    }

    @Override
    public ProjectProperty projectProperty()
    {
        return property;
    }

    @Override
    public Optional<?> resolveValue(Project project)
    {
        var omniFocusProject = repository.project(project.name());
        if (omniFocusProject == null)
        {
            return Optional.empty();
        }
        return Optional.of(omniFocusProject.link());
    }
}
