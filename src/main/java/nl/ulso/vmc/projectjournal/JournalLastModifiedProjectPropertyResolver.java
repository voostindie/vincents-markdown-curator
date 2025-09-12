package nl.ulso.vmc.projectjournal;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.markdown_curator.journal.Journal;
import nl.ulso.markdown_curator.project.*;

import java.util.Map;
import java.util.Optional;

import static nl.ulso.markdown_curator.project.ProjectProperty.LAST_MODIFIED;

/**
 * Resolves the last modification date of projects from the journal by looking at the most recent
 * reference to them in the daily journal entries.
 * <p/>
 * This resolver takes precedence over the standard front matter attribute resolver.
 */
@Singleton
public class JournalLastModifiedProjectPropertyResolver
        implements ProjectPropertyResolver
{
    private final Journal journal;
    private final ProjectProperty lastModifiedProperty;

    @Inject
    JournalLastModifiedProjectPropertyResolver(
            Journal journal,
            Map<String, ProjectProperty> projectProperties)
    {
        this.journal = journal;
        this.lastModifiedProperty = projectProperties.get(LAST_MODIFIED);
    }

    @Override
    public ProjectProperty projectProperty()
    {
        return lastModifiedProperty;
    }

    @Override
    public Optional<?> resolveValue(Project project)
    {
        return journal.mostRecentMentionOf(project.document().name());
    }

    @Override
    public int order()
    {
        return 1000;
    }
}
