package nl.ulso.vmc.projectjournal;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.markdown_curator.project.*;

import java.util.Map;
import java.util.Optional;

import static nl.ulso.markdown_curator.project.ProjectProperty.STATUS;

/// Resolves the status of projects from the journal by looking at the most recent use of a status
/// alias in the daily journal entries.
///
/// This resolver takes precedence over the standard front matter attribute resolver.
@Singleton
final class StatusResolver
    implements ValueResolver
{
    private final ProjectJournal projectJournal;
    private final ProjectProperty statusProperty;

    @Inject
    StatusResolver(ProjectJournal projectJournal, Map<String, ProjectProperty> projectProperties)
    {
        this.projectJournal = projectJournal;
        this.statusProperty = projectProperties.get(STATUS);
    }

    @Override
    public ProjectProperty property()
    {
        return statusProperty;
    }

    @Override
    public Optional<?> from(Project project)
    {
        return projectJournal.statusOf(project);
    }

    @Override
    public int order()
    {
        return 1;
    }
}
