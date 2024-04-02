package nl.ulso.vmc.project;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.markdown_curator.DataModelTemplate;
import nl.ulso.markdown_curator.journal.Journal;
import nl.ulso.markdown_curator.vault.*;
import nl.ulso.markdown_curator.vault.event.*;
import org.slf4j.Logger;

import java.util.*;

import static java.util.Comparator.comparing;
import static org.slf4j.LoggerFactory.getLogger;

@Singleton
public final class ProjectList
        extends DataModelTemplate
{
    private static final Logger LOGGER = getLogger(ProjectList.class);

    private final Journal journal;
    private final List<Project> projects;
    private final ProjectListSettings settings;

    @Inject
    ProjectList(Journal journal, ProjectListSettings settings)
    {
        this.journal = journal;
        this.settings = settings;
        this.projects = new ArrayList<>();
    }

    @Override
    protected void fullRefresh()
    {
        var finder = new ProjectFinder();
        journal.vault().accept(finder);
        projects.clear();
        projects.addAll(finder.projects);
        projects.sort(comparing(Project::lastModified).reversed());
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Built a project list of {} projects", projects.size());
        }
    }

    @Override
    public void process(FolderAdded event)
    {
        processDocumentChangeEvent(event.folder());
    }

    @Override
    public void process(FolderRemoved event)
    {
        processDocumentChangeEvent(event.folder());
    }

    @Override
    public void process(DocumentAdded event)
    {
        processDocumentChangeEvent(event.document().folder());
    }

    @Override
    public void process(DocumentChanged event)
    {
        processDocumentChangeEvent(event.document().folder());
    }

    @Override
    public void process(DocumentRemoved event)
    {
        processDocumentChangeEvent(event.document().folder());
    }

    private void processDocumentChangeEvent(Folder eventFolder)
    {
        var folder = eventFolder;
        while (folder != journal.vault())
        {
            if (folder.name().contentEquals(settings.projectFolder()))
            {
                fullRefresh();
            }
            folder = folder.parent();
        }
    }

    List<Project> projects()
    {
        return Collections.unmodifiableList(projects);
    }

    ProjectListSettings settings()
    {
        return settings;
    }

    private class ProjectFinder
            extends BreadthFirstVaultVisitor
    {
        private final List<Project> projects = new ArrayList<>();

        @Override
        public void visit(Vault vault)
        {
            vault.folder(settings.projectFolder()).ifPresent(
                    folder -> folder.documents()
                            .forEach(document -> document.accept(this)));
        }

        @Override
        public void visit(Document document)
        {
            journal.mostRecentMentionOf(document.name())
                    .ifPresent((date) -> projects.add(new Project(document, date)));
        }
    }
}