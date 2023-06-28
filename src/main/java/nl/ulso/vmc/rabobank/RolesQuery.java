package nl.ulso.vmc.rabobank;

import jakarta.inject.Inject;
import nl.ulso.markdown_curator.query.*;

import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparing;

class RolesQuery
        implements Query
{
    private final OrgChart orgChart;
    private final QueryResultFactory resultFactory;

    @Inject
    public RolesQuery(OrgChart orgChart, QueryResultFactory resultFactory)
    {
        this.orgChart = orgChart;
        this.resultFactory = resultFactory;
    }

    @Override
    public String name()
    {
        return "roles";
    }

    @Override
    public String description()
    {
        return "lists all roles of a contact";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return Collections.emptyMap();
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        var contact = definition.document().name();
        var roles = orgChart.forContact(contact).stream()
                .sorted(comparing(orgUnit -> orgUnit.team().sortableTitle()))
                .map(unit -> Map.of("Team", unit.team().link(),
                        "Role", unit.roles().entrySet().stream()
                                .filter(e -> e.getValue().containsKey(contact))
                                .map(Map.Entry::getKey).findFirst().orElse("")))
                .toList();
        return resultFactory.table(List.of("Team", "Role"), roles);
    }
}
