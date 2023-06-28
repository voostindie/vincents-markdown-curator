package nl.ulso.vmc.project;

import nl.ulso.markdown_curator.journal.Journal;
import nl.ulso.markdown_curator.query.*;
import nl.ulso.markdown_curator.vault.*;

import jakarta.inject.Inject;
import java.util.*;

import static java.util.Comparator.comparing;

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
        return "outputs all active projects";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return Map.of("format", "Output format: 'list' (default) or 'table'.");
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        var format = FORMATS.get(definition.configuration().string("format", "list"));
        if (format == null)
        {
            return resultFactory.error("Unsupported format");
        }
        var finder = new ProjectFinder(settings, journalModel);
        vault.accept(finder);
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
    }

    private static class ProjectFinder
            extends BreadthFirstVaultVisitor
    {
        private final List<Map<String, String>> projects = new ArrayList<>();
        private final ProjectListSettings settings;
        private final Journal journalModel;

        public ProjectFinder(ProjectListSettings settings, Journal journalModel)
        {
            this.settings = settings;
            this.journalModel = journalModel;
        }

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
            journalModel.mostRecentMentionOf(document.name()).ifPresent(
                    (date) -> projects.add(Map.of(
                            settings.projectColumn(), document.link(),
                            settings.dateColumn(), "[[" + date + "]]"
                    )));
        }
    }
}
