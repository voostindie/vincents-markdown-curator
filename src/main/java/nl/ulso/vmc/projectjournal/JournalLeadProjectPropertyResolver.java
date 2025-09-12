package nl.ulso.vmc.projectjournal;

import jakarta.inject.*;
import nl.ulso.markdown_curator.project.*;

import java.util.Map;
import java.util.Optional;

import static nl.ulso.markdown_curator.project.ProjectProperty.LEAD;

/**
 * Resolves the leads of projects from the journal by looking at the most recent use of a lead
 * alias in the daily journal entries.
 * <p/>
 * This resolver takes precedence over the standard front matter attribute resolver.
 */
@Singleton
public class JournalLeadProjectPropertyResolver
        implements ProjectPropertyResolver
{
    private final ProjectJournal projectJournal;
    private final ProjectProperty leadProperty;

    @Inject
    JournalLeadProjectPropertyResolver(
            ProjectJournal projectJournal, Map<String, ProjectProperty> projectProperties)
    {
        this.projectJournal = projectJournal;
        this.leadProperty = projectProperties.get(LEAD);
    }

    @Override
    public ProjectProperty projectProperty()
    {
        return leadProperty;
    }

    @Override
    public Optional<?> resolveValue(Project project)
    {
        return projectJournal.leadOf(project);
    }

    @Override
    public int order()
    {
        return 1;
    }
}
