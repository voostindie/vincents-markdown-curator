package nl.ulso.vmc.tweevv;

import nl.ulso.markdown_curator.query.*;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

public class VolunteersQuery
        implements Query
{
    private final VolunteeringModel model;
    private final QueryResultFactory resultFactory;

    @Inject
    VolunteersQuery(VolunteeringModel model, QueryResultFactory resultFactory)
    {
        this.model = model;
        this.resultFactory = resultFactory;
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
            return resultFactory.error("Property 'season' is not specified");
        }
        var list = model.volunteersFor(season).entrySet().stream()
                .map(entry -> Map.of("Vrijwilliger", entry.getKey().link(),
                        "Taak", entry.getValue().stream()
                                .sorted(comparing(VolunteeringModel.ContactActivity::description))
                                .map(VolunteeringModel.ContactActivity::description)
                                .collect(Collectors.joining(", "))))
                .sorted(comparing(map -> map.get("Vrijwilliger")))
                .toList();
        return resultFactory.table(List.of("Vrijwilliger", "Taak"), list);
    }
}
