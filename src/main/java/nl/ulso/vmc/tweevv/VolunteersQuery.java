package nl.ulso.vmc.tweevv;

import nl.ulso.markdown_curator.query.*;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static nl.ulso.markdown_curator.query.QueryResult.empty;
import static nl.ulso.markdown_curator.query.QueryResult.error;
import static nl.ulso.markdown_curator.query.QueryResult.table;

public class VolunteersQuery
        implements Query
{
    private final VolunteeringModel model;

    @Inject
    VolunteersQuery(VolunteeringModel model)
    {
        this.model = model;
    }

    @Override
    public String name()
    {
        return "volunteers";
    }

    @Override
    public String description()
    {
        return "Lists all volunteers for a specific season.";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return Map.of("season", "Season to list; e.g. '2021-2022'.");
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        var season = definition.configuration().string("season", null);
        if (season == null)
        {
            return error("Property 'season' is not specified");
        }
        var list = model.volunteersFor(season).entrySet().stream()
                .map(entry -> Map.of("Vrijwilliger", entry.getKey().link(),
                        "Taak", entry.getValue().stream()
                                .sorted(comparing(VolunteeringModel.Activity::name))
                                .map(VolunteeringModel.Activity::toMarkdown)
                                .collect(Collectors.joining(", "))))
                .sorted(comparing(map -> map.get("Vrijwilliger")))
                .toList();
        if (list.isEmpty())
        {
            return empty();
        }
        return table(List.of("Vrijwilliger", "Taak"), list);
    }
}
