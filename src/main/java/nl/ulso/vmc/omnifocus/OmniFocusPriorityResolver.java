package nl.ulso.vmc.omnifocus;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.markdown_curator.project.*;

import java.util.Map;
import java.util.Optional;

import static nl.ulso.markdown_curator.project.ProjectProperty.PRIORITY;

/**
 * Resolves the priority of a project from OmniFocus.
 * <p/>
 * This resolver takes the highest precedence.
 */
@Singleton
final class OmniFocusPriorityResolver
        implements ProjectPropertyResolver
{
    private final OmniFocusRepository omniFocusRepository;
    private final ProjectProperty priorityProperty;

    @Inject
    OmniFocusPriorityResolver(
            OmniFocusRepository omnifocusRepository,
            Map<String, ProjectProperty> projectProperties)
    {
        this.omniFocusRepository = omnifocusRepository;
        this.priorityProperty = projectProperties.get(PRIORITY);
    }

    @Override
    public ProjectProperty projectProperty()
    {
        return priorityProperty;
    }

    @Override
    public Optional<?> resolveValue(Project project)
    {
        var priority = omniFocusRepository.priorityOf(project.document().name());
        if (priority >= 0)
        {
            return Optional.of(priority);
        }
        return Optional.empty();
    }

    @Override
    public int order()
    {
        return 0;
    }
}
