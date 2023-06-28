package nl.ulso.vmc.tweevv;

import nl.ulso.markdown_curator.query.*;

import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

public class GroupQuery
        implements Query
{
    private final VolunteeringModel model;
    private final QueryResultFactory resultFactory;

    @Inject
    public GroupQuery(VolunteeringModel model, QueryResultFactory resultFactory)
    {
        this.model = model;
        this.resultFactory = resultFactory;
    }

    @Override
    public String name()
    {
        return "group";
    }

    @Override
    public String description()
    {
        return "Lists all volunteers for an activity.";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return Map.of(
                "season", "Season to list the people for; defaults to the current season",
                "name", "Name of the activity; defaults to the current document name",
                "format", "\"list\" or \"table\"; defaults to \"list\""
        );
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        var seasonString = definition.configuration().string("season", currentSeason());
        var group = definition.configuration().string("name", definition.document().name());
        var format = definition.configuration().string("format", "list");
        var table = model.volunteersFor(seasonString, group).entrySet().stream()
                .map(entry -> Map.of("Vrijwilliger", entry.getKey().link(),
                        "Rol", entry.getValue().stream()
                                .sorted(comparing(VolunteeringModel.ContactActivity::description))
                                .map(VolunteeringModel.ContactActivity::shortDescription)
                                .collect(Collectors.joining(", "))))
                .sorted(comparing(map -> map.get("Vrijwilliger")))
                .toList();
        if (format.contentEquals("table"))
        {
            return resultFactory.table(List.of("Vrijwilliger", "Rol"), table);
        }
        var list = table.stream().map(map -> map.get("Vrijwilliger")).toList();
        return resultFactory.unorderedList(list);
    }

    private String currentSeason()
    {
        var date = LocalDate.now();
        var year = date.getYear();
        if (date.getMonth().getValue() < 9)
        {
            year--;
        }
        return year + "-" + (year + 1);
    }
}
