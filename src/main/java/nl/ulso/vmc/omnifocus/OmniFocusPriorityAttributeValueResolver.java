package nl.ulso.vmc.omnifocus;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.markdown_curator.project.*;

import java.util.Optional;

import static nl.ulso.markdown_curator.project.Attribute.PRIORITY;

/**
 * Resolves the priority of a project from OmniFocus.
 * <p/>
 * This resolver takes precedence over the standard front matter attribute resolver.
 */
@Singleton
public class OmniFocusPriorityAttributeValueResolver
        implements AttributeValueResolver<Integer>
{
    private final OmniFocusRepository omniFocusRepository;

    @Inject
    OmniFocusPriorityAttributeValueResolver(OmniFocusRepository omnifocusRepository)
    {
        this.omniFocusRepository = omnifocusRepository;
    }

    @Override
    public Attribute<Integer> attribute()
    {
        return PRIORITY;
    }

    @Override
    public Optional<Integer> resolveValue(Project project)
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
