package nl.ulso.vmc.project;

import nl.ulso.markdown_curator.query.*;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * Lists all projects lead by a specific party (contact, team, ...document).
 * <p/>
 * Basically this is a filter on top of the ProjectListQuery, selecting only those projects that
 * belong to a specific lead.
 */
public final class ProjectLeadQuery
        implements Query
{
    private final ProjectList projectList;
    private final QueryResultFactory resultFactory;

    @Inject
    public ProjectLeadQuery(ProjectList projectList, QueryResultFactory resultFactory)
    {
        this.projectList = projectList;
        this.resultFactory = resultFactory;
    }

    @Override
    public String name()
    {
        return "projectlead";
    }

    @Override
    public String description()
    {
        return "Outputs all active projects lead by a specific party.";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return Map.of("lead", "Project lead to select; defaults to the name of the document.");
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        var lead = definition.configuration()
                .string("lead", "[[" + definition.document().name() + "]]");
        var settings = projectList.settings();
        var projects = projectList.projects();
        return resultFactory.table(
                List.of(settings.projectColumn(), settings.dateColumn(), settings.statusColumn()),
                projects.stream()
                        .filter((Project project) -> lead.contentEquals(project.leadWikiLink()))
                        .map((Project project) -> Map.of(
                                        settings.dateColumn(), "[[" + project.lastModified() + "]]",
                                        settings.projectColumn(), "[[" + project.name() + "]]",
                                        settings.statusColumn(), project.status().toMarkdown()
                                )
                        )
                        .toList()
        );
    }
}
