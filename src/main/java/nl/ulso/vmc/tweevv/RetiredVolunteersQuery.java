package nl.ulso.vmc.tweevv;

import jakarta.inject.Inject;
import nl.ulso.markdown_curator.Changelog;
import nl.ulso.markdown_curator.query.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

public class RetiredVolunteersQuery
    implements Query
{
    private final VolunteeringModel model;
    private final QueryResultFactory resultFactory;

    @Inject
    RetiredVolunteersQuery(VolunteeringModel model, QueryResultFactory resultFactory)
    {
        this.model = model;
        this.resultFactory = resultFactory;
    }

    @Override
    public String name()
    {
        return "retiredvolunteers";
    }

    @Override
    public String description()
    {
        return "Lists all volunteers that retired after the prior season.";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return Map.of("season", "Season to list; e.g. '2025-2026'.");
    }

    @Override
    public boolean isImpactedBy(Changelog changelog, QueryDefinition definition)
    {
        return changelog.changes().anyMatch(model.isContactDocument());
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        var season = definition.configuration().string("season", null);
        if (season == null)
        {
            return resultFactory.error("Property 'season' is not specified");
        }
        var list = model.retiredVolunteersFor(season).entrySet().stream()
            .map(entry -> Map.of(
                    "Vrijwilliger", entry.getKey().link(),
                    "Taak voorgaand seizoen", entry.getValue().stream()
                        .sorted(comparing(VolunteeringModel.ContactActivity::description))
                        .map(VolunteeringModel.ContactActivity::description)
                        .collect(Collectors.joining(", "))
                )
            )
            .sorted(comparing(map -> map.get("Vrijwilliger")))
            .toList();
        return resultFactory.table(List.of("Vrijwilliger", "Taak voorgaand seizoen"), list);
    }
}
