package nl.ulso.vmc.projectjournal;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.markdown_curator.project.*;

import java.util.Optional;

import static nl.ulso.markdown_curator.project.Attribute.STATUS;

/**
 * Resolves the status of projects from the journal by looking at the most recent use of a status
 * alias in the daily journal entries.
 * <p/>
 * This resolver takes precedence over the standard front matter attribute resolver.
 */
@Singleton
public class JournalStatusAttributeValueResolver
        implements AttributeValueResolver<String>
{
    private final ProjectJournal projectJournal;

    @Inject
    JournalStatusAttributeValueResolver(ProjectJournal projectJournal)
    {
        this.projectJournal = projectJournal;
    }

    @Override
    public Attribute<String> attribute()
    {
        return STATUS;
    }

    @Override
    public Optional<String> resolveValue(Project project)
    {
        return projectJournal.statusOf(project);
    }

    @Override
    public int order()
    {
        return 1;
    }
}
