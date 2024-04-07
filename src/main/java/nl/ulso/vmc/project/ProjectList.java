package nl.ulso.vmc.project;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.markdown_curator.DataModelTemplate;
import nl.ulso.markdown_curator.journal.Journal;
import nl.ulso.markdown_curator.vault.*;
import nl.ulso.markdown_curator.vault.event.*;
import nl.ulso.vmc.omnifocus.OmniFocusRepository;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparing;
import static org.slf4j.LoggerFactory.getLogger;

@Singleton
public final class ProjectList
        extends DataModelTemplate
{
    private static final Logger LOGGER = getLogger(ProjectList.class);

    private final Journal journal;
    private final List<Project> projects;
    private final ProjectListSettings projectListSettings;
    private final OmniFocusRepository omniFocusRepository;

    @Inject
    ProjectList(
            Journal journal, ProjectListSettings projectListSettings,
            OmniFocusRepository omniFocusRepository)
    {
        this.journal = journal;
        this.projectListSettings = projectListSettings;
        this.omniFocusRepository = omniFocusRepository;
        this.projects = new ArrayList<>();
    }

    @Override
    protected void fullRefresh()
    {
        var finder = new ProjectFinder();
        journal.vault().accept(finder);
        projects.clear();
        projects.addAll(finder.projects);
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
            if (folder.name().contentEquals(projectListSettings.projectFolder()))
            {
                fullRefresh();
            }
            folder = folder.parent();
        }
    }

    List<Project> projects()
    {
        var list = new ArrayList<>(projects);
        list.sort(comparing(Project::priority));
        return unmodifiableList(list);
    }

    ProjectListSettings settings()
    {
        return projectListSettings;
    }

    private class ProjectFinder
            extends BreadthFirstVaultVisitor
    {
        private final List<Project> projects = new ArrayList<>();

        @Override
        public void visit(Vault vault)
        {
            vault.folder(projectListSettings.projectFolder()).ifPresent(
                    folder -> folder.documents()
                            .forEach(document -> document.accept(this)));
        }

        @Override
        public void visit(Document document)
        {
            journal.mostRecentMentionOf(document.name())
                    .ifPresent((date) -> projects.add(
                            new Project(document, date, omniFocusRepository)));
        }
    }
}