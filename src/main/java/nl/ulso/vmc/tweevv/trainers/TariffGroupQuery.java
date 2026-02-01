package nl.ulso.vmc.tweevv.trainers;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.query.*;

import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static nl.ulso.curator.query.TableResult.Alignment.LEFT;
import static nl.ulso.curator.query.TableResult.Alignment.RIGHT;

/**
 * Generates a table of all tariff groups in a season, including the training groups that qualify
 * for it.
 */
@Singleton
public class TariffGroupQuery
    extends SeasonQueryTemplate
{
    private static final String TARIFF_GROUP_COLUMN = "Tariefgroep";
    private static final String TARIFF_COLUMN = "Tarief";
    private static final String TRAINING_GROUP_COLUMN = "Trainingsgroepen";

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
            List.of(TARIFF_GROUP_COLUMN, TARIFF_COLUMN, TRAINING_GROUP_COLUMN),
            List.of(LEFT, RIGHT, LEFT),
            season.tariffGroups()
                .sorted(comparing(TariffGroup::tariff).thenComparing(TariffGroup::name))
                .map(tariffGroup ->
                    Map.of(
                        TARIFF_GROUP_COLUMN, tariffGroup.link(),
                        TARIFF_COLUMN, toEuroString(tariffGroup.tariff()),
                        TRAINING_GROUP_COLUMN, season.trainingGroupsFor(tariffGroup)
                            .sorted(comparing(TrainingGroup::name))
                            .map(TrainingGroup::link)
                            .collect(joining(", "))
                    )
                )
                .toList()
        );
    }
}
