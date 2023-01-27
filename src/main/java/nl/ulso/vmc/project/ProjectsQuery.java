package nl.ulso.vmc.project;

import nl.ulso.markdown_curator.journal.Journal;
import nl.ulso.markdown_curator.query.*;
import nl.ulso.markdown_curator.vault.*;

import javax.inject.Inject;
import java.util.*;
import java.util.regex.Pattern;

import static java.util.Comparator.comparing;
import static java.util.regex.Pattern.compile;

public class ProjectsQuery
        implements Query
{
    private final Vault vault;
    private final ProjectListSettings settings;
    private final QueryResultFactory resultFactory;
    private final Journal journalModel;

    private enum Format
    {
        LIST,
        TABLE
    }

    private final static Map<String, Format> FORMATS =
            Map.of("list", Format.LIST, "table", Format.TABLE);

    @Inject
    public ProjectsQuery(
            Vault vault, ProjectListSettings settings, QueryResultFactory resultFactory,
            Journal journalModel)
    {
        this.vault = vault;
        this.settings = settings;
        this.resultFactory = resultFactory;
        this.journalModel = journalModel;
    }

    @Override
    public String name()
    {
        return "projects";
    }

    @Override
    public String description()
    {
        return "outputs all active projects in a table";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return Map.of("format", "Output format: 'table' (default) or 'list'.");
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        var format = FORMATS.get(definition.configuration().string("format", "table"));
        if (format == null)
        {
            return resultFactory.error("Unsupported format");
        }
        return vault.folder(settings.projectFolder()).map(folder ->
        {
            var finder = new ProjectFinder(settings, journalModel);
            folder.accept(finder);
            var projects = finder.projects;
            projects.sort(
                    comparing((Map<String, String> e) -> e.get(settings.dateColumn())).reversed());
            return switch (format)
                    {
                        case LIST -> resultFactory.unorderedList(projects.stream()
                                .map((Map<String, String> e) ->
                                        e.get(settings.dateColumn()) + ": " +
                                        e.get(settings.projectColumn()))
                                .toList());
                        case TABLE -> resultFactory.table(
                                List.of(settings.dateColumn(), settings.projectColumn()),
                                projects);
                    };
        }).orElseGet(() -> resultFactory.error(
                "Couldn't find the folder '" + settings.projectFolder() + "'"));
    }

    private static class ProjectFinder
            extends BreadthFirstVaultVisitor
    {
        private static final Pattern TITLE_PATTERN =
                compile("^\\[\\[(\\d{4}-\\d{2}-\\d{2})]]: (.*)$");

        private final List<Map<String, String>> projects = new ArrayList<>();
        private final ProjectListSettings settings;
        private final Journal journalModel;

        public ProjectFinder(ProjectListSettings settings, Journal journalModel)
        {
            this.settings = settings;
            this.journalModel = journalModel;
        }

        @Override
        public void visit(Folder folder)
        {
            // Don't recurse into subfolders!
            if (folder.name().contentEquals(settings.projectFolder()))
            {
                super.visit(folder);
            }
        }

        @Override
        public void visit(Document document)
        {
            journalModel.mostRecentMentionOf(document.name()).ifPresentOrElse(
                    (date) -> projects.add(Map.of(
                            settings.projectColumn(), document.link(),
                            settings.dateColumn(), "[[" + date + "]]"
                    )),
                    () -> super.visit(document)
            );
        }

        /**
         * Extracts the last modification date for a project from the activity list.
         * THIS IS A LEGACY METHDO!
         * Once all active projects have been mentioned in a daily note, this code is no longer
         * touched, being replaced by the Journal. At that point in time this method
         * and the code calling it (see "super.visit(document)" above) can go.
         */
        @Override
        public void visit(Section section)
        {
            if (section.level() == 2
                && section.sortableTitle().contentEquals(settings.timelineSection())
                && section.fragments().size() > 1
                && section.fragments().get(1) instanceof Section subsection)
            {
                var matcher = TITLE_PATTERN.matcher(subsection.title());
                if (matcher.matches())
                {
                    projects.add(Map.of(
                            settings.projectColumn(), section.document().link(),
                            settings.dateColumn(), matcher.group(1)
                    ));
                }
            }
        }
    }
}
