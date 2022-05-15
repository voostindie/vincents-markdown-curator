package nl.ulso.vmc.project;

import nl.ulso.markdown_curator.query.Query;
import nl.ulso.markdown_curator.query.QueryResult;
import nl.ulso.markdown_curator.vault.*;

import java.util.*;
import java.util.regex.Pattern;

import static java.util.Collections.emptyMap;
import static java.util.Comparator.comparing;
import static java.util.regex.Pattern.compile;
import static nl.ulso.markdown_curator.query.QueryResult.error;
import static nl.ulso.markdown_curator.query.QueryResult.table;

public class ProjectListQuery
        implements Query
{
    private final Vault vault;
    private final ProjectListSettings settings;

    public ProjectListQuery(Vault vault, ProjectListSettings settings)
    {
        this.vault = vault;
        this.settings = settings;
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
        return emptyMap();
    }

    @Override
    public QueryResult run(QueryBlock queryBlock)
    {
        return vault.folder(settings.projectFolder()).map(folder -> {
            var finder = new ProjectFinder(settings);
            folder.accept(finder);
            var projects = finder.projects;
            projects.sort(
                    comparing((Map<String, String> e) -> e.get(settings.dateColumn())).reversed());
            return table(List.of(settings.dateColumn(), settings.projectColumn()), projects);
        }).orElse(error("Couldn't find the folder '" + settings.projectFolder() + "'"));
    }

    private static class ProjectFinder
            extends BreadthFirstVaultVisitor
    {
        private static final Pattern TITLE_PATTERN =
                compile("^\\[\\[(\\d{4}-\\d{2}-\\d{2})]]: (.*)$");

        private final List<Map<String, String>> projects = new ArrayList<>();
        private final ProjectListSettings settings;

        public ProjectFinder(ProjectListSettings settings)
        {
            this.settings = settings;
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
        public void visit(Section section)
        {
            if (section.level() == 2 && section.title().contentEquals(settings.timelineSection())
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
