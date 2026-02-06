package nl.ulso.vmc.rabobank;

import jakarta.inject.Inject;
import nl.ulso.curator.change.Changelog;
import nl.ulso.curator.query.*;
import nl.ulso.curator.vault.Document;

import java.util.*;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static nl.ulso.curator.change.Change.isPayloadType;

class SubteamsQuery
    implements Query
{
    private final OrgChart orgChart;
    private final QueryResultFactory resultFactory;

    @Inject
    public SubteamsQuery(OrgChart orgChart, QueryResultFactory resultFactory)
    {
        this.orgChart = orgChart;
        this.resultFactory = resultFactory;
    }

    @Override
    public String name()
    {
        return "subteams";
    }

    @Override
    public String description()
    {
        return "Lists all subteams of a team.";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return Map.of("style", "list or table; defaults to list"
            , "roles", "names of the roles in the table"
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
        var parent = definition.document().name();
        var style = definition.configuration().string("style", "list");
        switch (style)
        {
            case "list" ->
            {
                var units = orgChart.forParent(parent).stream()
                    .map(OrgUnit::team)
                    .sorted(comparing(Document::sortableTitle))
                    .map(Document::link)
                    .toList();
                return resultFactory.unorderedList(units);
            }
            case "table" ->
            {
                var roles = definition.configuration().listOfStrings("roles");
                var rows = orgChart.forParent(parent).stream()
                    .sorted(comparing(orgUnit -> orgUnit.team().sortableTitle()))
                    .map(orgUnit ->
                    {
                        Map<String, String> row = new HashMap<>();
                        row.put("Name", orgUnit.team().link());
                        for (String role : roles)
                        {
                            var contactMap = orgUnit.roles().get(role);
                            if (contactMap != null)
                            {
                                row.put(role, contactMap.values().stream()
                                    .map(Document::link).collect(joining(", "))
                                );
                            }
                        }
                        return row;
                    }).toList();
                var columns = new ArrayList<String>(roles.size() + 1);
                columns.add("Name");
                columns.addAll(roles);
                return resultFactory.table(columns, rows);
            }
            default ->
            {
                return resultFactory.error("Unsupported style: " + style);
            }
        }
    }
}
