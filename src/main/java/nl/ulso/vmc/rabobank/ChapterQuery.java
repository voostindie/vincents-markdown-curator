package nl.ulso.vmc.rabobank;

import nl.ulso.markdown_curator.query.*;
import nl.ulso.markdown_curator.vault.Document;

import javax.inject.Inject;
import java.util.Map;

public class ChapterQuery
        implements Query
{
    private final OrgChart orgChart;
    private final QueryResultFactory resultFactory;

    @Inject
    public ChapterQuery(OrgChart orgChart, QueryResultFactory resultFactory)
    {
        this.orgChart = orgChart;
        this.resultFactory = resultFactory;
    }

    @Override
    public String name()
    {
        return "chapter";
    }

    @Override
    public String description()
    {
        return "Generates an overview of people in a chapter.";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return Map.of("roles",
                "Roles of the people in the chapter. Roles are matched on substrings in lower " +
                "case, e.g. 'architect' is enough to find 'Solution Architect'.",
                "teams",
                "List of teams to look in, hierarchically. Teams are matched on substrings in " +
                "lower case.");
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        var roles = definition.configuration().listOfStrings("roles");
        if (roles.isEmpty())
        {
            return resultFactory.error("'roles' is required");
        }
        var teams = definition.configuration().listOfStrings("teams");
        if (teams.isEmpty())
        {
            return resultFactory.error("'teams' is required");
        }
        var contacts = orgChart.chapterFor(roles, teams);
        return resultFactory.unorderedList(contacts.stream().map(Document::link).toList());
    }
}
