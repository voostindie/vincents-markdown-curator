package nl.ulso.vmc.rabobank;

import nl.ulso.markdown_curator.query.*;
import nl.ulso.markdown_curator.vault.Document;

import javax.inject.Inject;
import java.util.*;

import static java.util.stream.Collectors.joining;
import static nl.ulso.markdown_curator.query.QueryResult.error;
import static nl.ulso.markdown_curator.query.QueryResult.unorderedList;

class SubteamsQuery
        implements Query
{
    private final OrgChart orgChart;

    @Inject
    public SubteamsQuery(OrgChart orgChart)
    {
        this.orgChart = orgChart;
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
                , "roles", "names of the roles in the table");
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        var parent = definition.document().name();
        var style = definition.configuration().string("style", "list");
        switch (style)
        {
            case "list":
                var units = orgChart.forParent(parent).stream()
                        .map(OrgUnit::team)
                        .map(Document::link)
                        .sorted()
                        .toList();
                return unorderedList(units);
            case "table":
                var roles = definition.configuration().listOfStrings("roles");
                var rows = orgChart.forParent(parent).stream()
                        .sorted(Comparator.comparing(orgUnit -> orgUnit.team().name()))
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
                                            .map(Document::link).collect(joining(", ")));
                                }
                            }
                            return row;
                        }).toList();
                var columns = new ArrayList<String>(roles.size() + 1);
                columns.add("Name");
                columns.addAll(roles);
                return QueryResult.table(columns, rows);
            default:
                return error("Unsupported style: " + style);
        }
    }
}
