package nl.ulso.vmc.omnifocus;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.markdown_curator.project.*;

import java.util.Optional;

import static nl.ulso.markdown_curator.project.Attribute.STATUS;

/**
 * Resolves the status of projects that are on hold in OmniFocus.
 * <p/>
 * This resolver takes precedence over the standard front matter attribute resolver. It acts
 * only if the project is on hold in OmniFocus.
 */
@Singleton
final class OmniFocusStatusAttributeValueResolver
        implements AttributeValueResolver<String>
{
    private final OmniFocusRepository omniFocusRepository;
    private final OmniFocusMessages messages;

    @Inject
    OmniFocusStatusAttributeValueResolver(
            OmniFocusRepository omnifocusRepository, OmniFocusMessages messages)
    {
        this.omniFocusRepository = omnifocusRepository;
        this.messages = messages;
    }

    @Override
    public Attribute<String> attribute()
    {
        return STATUS;
    }

    @Override
    public Optional<String> resolveValue(Project project)
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
