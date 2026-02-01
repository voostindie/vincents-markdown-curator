package nl.ulso.vmc.rabobank;

import jakarta.inject.Inject;
import nl.ulso.curator.changelog.Changelog;
import nl.ulso.curator.query.*;
import nl.ulso.curator.vault.Document;

import java.util.List;
import java.util.Map;

import static nl.ulso.curator.changelog.Change.isPayloadType;

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
            "lower case."
        );
    }

    @Override
    public boolean isImpactedBy(Changelog changelog, QueryDefinition definition)
    {
        return changelog.changes().anyMatch(
            isPayloadType(Document.class).and(orgChart.isFolderInScope()));
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        var roles = resolveRoles(definition);
        if (roles.isEmpty())
        {
            return resultFactory.error("'roles' is required");
        }
        var teams = resolveTeams(definition);
        if (teams.isEmpty())
        {
            return resultFactory.error("'teams' is required");
        }
        var contacts = orgChart.chapterFor(roles, teams);
        return resultFactory.unorderedList(contacts.stream().map(Document::link).toList());
    }

    private List<String> resolveTeams(QueryDefinition definition)
    {
        return definition.configuration().listOfStrings("teams");
    }

    private List<String> resolveRoles(QueryDefinition definition)
    {
        return definition.configuration().listOfStrings("roles");
    }
}
