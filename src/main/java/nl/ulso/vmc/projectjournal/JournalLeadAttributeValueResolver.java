package nl.ulso.vmc.projectjournal;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.markdown_curator.project.*;
import nl.ulso.markdown_curator.vault.Document;

import java.util.Optional;

import static nl.ulso.markdown_curator.project.Attribute.LEAD;

/**
 * Resolves the leads of projects from the journal by looking at the most recent use of a lead
 * alias in the daily journal entries.
 * <p/>
 * This resolver takes precedence over the standard front matter attribute resolver.
 */
@Singleton
public class JournalLeadAttributeValueResolver
        implements AttributeValueResolver<Document>
{
    private final ProjectJournal projectJournal;

    @Inject
    JournalLeadAttributeValueResolver(ProjectJournal projectJournal)
    {
        this.projectJournal = projectJournal;
    }

    @Override
    public Attribute<Document> attribute()
    {
        return LEAD;
    }

    @Override
    public Optional<Document> resolveValue(Project project)
    {
        return projectJournal.leadOf(project);
    }

    @Override
    public int order()
    {
        return 1;
    }
}
