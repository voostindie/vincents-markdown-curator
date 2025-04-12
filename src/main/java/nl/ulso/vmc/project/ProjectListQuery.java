package nl.ulso.vmc.project;

import nl.ulso.markdown_curator.query.*;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

public final class ProjectListQuery
        implements Query
{
    private final ProjectList projectList;
    private final QueryResultFactory resultFactory;

    private enum Format
    {
        LIST,
        TABLE
    }

    private final static Map<String, Format> FORMATS =
            Map.of("list", Format.LIST, "table", Format.TABLE);

    @Inject
    public ProjectListQuery(ProjectList projectList, QueryResultFactory resultFactory)
    {
        this.projectList = projectList;
        this.resultFactory = resultFactory;
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
        var settings = projectList.settings();
        var projects = projectList.projects();
        return switch (format)
        {
            case LIST -> resultFactory.unorderedList(projects.stream()
                    .map((Project project) -> "[[" + project.name() + "]]")
                    .toList());
            case TABLE -> resultFactory.table(
                    List.of(settings.priorityColumn(),
                            settings.projectColumn(),
                            settings.leadColumn(),
                            settings.dateColumn(),
                            settings.statusColumn()),
                    projects.stream()
                            .map((Project project) -> Map.of(
                                    settings.priorityColumn(), Integer.toString(project.priority()),
                                    settings.projectColumn(), "[[" + project.name() + "]]",
                                    settings.leadColumn(), project.leadWikiLink(),
                                    settings.dateColumn(), "[[" + project.lastModified().toString() + "]]",
                                    settings.statusColumn(), project.status().toMarkdown()
                            )).toList());
        };
    }
}
