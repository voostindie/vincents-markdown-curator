package nl.ulso.vmc.projectjournal;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.markdown_curator.journal.Journal;
import nl.ulso.markdown_curator.project.*;

import java.time.LocalDate;
import java.util.Optional;

import static nl.ulso.markdown_curator.project.Attribute.LAST_MODIFIED;

/**
 * Resolves the last modification date of projects from the journal by looking at the most recent
 * reference to them in the daily journal entries.
 * <p/>
 * This resolver takes precedence over the standard front matter attribute resolver.
 */
@Singleton
public class JournalLastModifiedAttributeValueResolver
        implements AttributeValueResolver<LocalDate>
{
    private final Journal journal;

    @Inject
    JournalLastModifiedAttributeValueResolver(Journal journal)
    {
        this.journal = journal;
    }

    @Override
    public Attribute<LocalDate> attribute()
    {
        return LAST_MODIFIED;
    }

    @Override
    public Optional<LocalDate> resolveValue(Project project)
    {
        return journal.mostRecentMentionOf(project.document().name());
    }

    @Override
    public int order()
    {
        return 1000;
    }
}
