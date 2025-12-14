package nl.ulso.vmc.tweevv.trainers;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.markdown_curator.query.*;

import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static nl.ulso.markdown_curator.query.TableResult.Alignment.LEFT;
import static nl.ulso.markdown_curator.query.TableResult.Alignment.RIGHT;

@Singleton
public class TariffGroupQuery
        extends SeasonQueryTemplate
{
    @Inject
    TariffGroupQuery(TrainerModel trainerModel, QueryResultFactory queryResultFactory)
    {
        super(trainerModel, queryResultFactory);
    }

    @Override
    public String name()
    {
        return "tariffgroups";
    }

    @Override
    public String description()
    {
        return "List all tariff groups in a season";
    }

    @Override
    protected QueryResult runFor(Season season, QueryDefinition definition)
    {
        return queryResultFactory().table(
                List.of("Tariefgroep", "Tarief", "Trainingsgroepen"),
                List.of(LEFT, RIGHT, LEFT),
                season.tariffGroups()
                        .sorted(comparing(TariffGroup::tariff)
                                .thenComparing(TariffGroup::name))
                        .map(tariffGroup -> Map.of(
                                "Tariefgroep", tariffGroup.link(),
                                "Tarief", toEuroString(tariffGroup.tariff()),
                                "Trainingsgroepen", season.trainingGroupsFor(tariffGroup)
                                        .sorted(comparing(TrainingGroup::name))
                                        .map(TrainingGroup::link)
                                        .collect(joining(", "))
                        ))
                        .toList()
        );
    }
}
