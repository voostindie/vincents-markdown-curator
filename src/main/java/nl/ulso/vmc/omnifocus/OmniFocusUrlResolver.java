package nl.ulso.vmc.omnifocus;

import nl.ulso.markdown_curator.project.*;

import java.util.Optional;

public class OmniFocusUrlResolver
        implements ProjectPropertyResolver
{
    public static final String OMNIFOCUS_URL = "omnifocus";
    private final ProjectProperty property;
    private final OmniFocusRepository repository;

    public OmniFocusUrlResolver(ProjectProperty property, OmniFocusRepository repository)
    {
        this.property = property;
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
