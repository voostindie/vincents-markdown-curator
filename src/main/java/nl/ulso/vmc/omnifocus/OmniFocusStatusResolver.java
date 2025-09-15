package nl.ulso.vmc.omnifocus;

import jakarta.inject.*;
import nl.ulso.markdown_curator.project.*;

import java.util.Map;
import java.util.Optional;

import static nl.ulso.markdown_curator.project.ProjectProperty.STATUS;

/**
 * Resolves the status of projects that are on hold in OmniFocus.
 * <p/>
 * This resolver takes precedence over the standard front matter property resolver. It acts
 * only if the project is on hold in OmniFocus.
 */
@Singleton
final class OmniFocusStatusResolver
        implements ProjectPropertyResolver
{
    private final OmniFocusRepository omniFocusRepository;
    private final ProjectProperty statusProperty;
    private final OmniFocusMessages messages;

    @Inject
    OmniFocusStatusResolver(
            OmniFocusRepository omnifocusRepository,
            Map<String, ProjectProperty> projectProperties,
            OmniFocusMessages messages)
    {
        this.omniFocusRepository = omnifocusRepository;
        this.statusProperty = projectProperties.get(STATUS);
        this.messages = messages;
    }

    @Override
    public ProjectProperty projectProperty()
    {
        return statusProperty;
    }

    @Override
    public Optional<?> resolveValue(Project project)
    {
        var status = omniFocusRepository.statusOf(project.document().name());
        if (status == Status.ON_HOLD)
        {
            return Optional.of(messages.projectOnHold());
        }
        return Optional.empty();
    }

    @Override
    public int order()
    {
        return 0;
    }
}
