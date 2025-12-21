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

/**
 * Generates a table of all traing groups in a season, including their trainers.
 */
@Singleton
public final class TrainingGroupQuery
    extends SeasonQueryTemplate
{
    private static final String TRAINING_GROUP_COLUMN = "Trainingsgroep";
    private static final String TARIFF_GROEP_COLUMN   = "Tariefgroep";
    private static final String PRACTICES_COLUMN      = "Trainingen";
    private static final String TRAINER_COLUMN        = "Trainer(s)";

    @Inject
    TrainingGroupQuery(TrainerModel trainerModel, QueryResultFactory queryResultFactory)
    {
        super(trainerModel, queryResultFactory);
    }

    @Override
    public String name()
    {
        return "traininggroups";
    }

    @Override
    public String description()
    {
        return "List all training groups in a season";
    }

    @Override
    protected QueryResult runFor(Season season, QueryDefinition definition)
    {
        return queryResultFactory().table(
            List.of(TRAINING_GROUP_COLUMN, TARIFF_GROEP_COLUMN, PRACTICES_COLUMN, TRAINER_COLUMN),
            List.of(LEFT, LEFT, RIGHT, LEFT),
            season.trainingGroups()
                .sorted(comparing(TrainingGroup::name))
                .map(trainingGroup ->
                    Map.of(
                        TRAINING_GROUP_COLUMN, trainingGroup.link(),
                        TARIFF_GROEP_COLUMN, trainingGroup.tariffGroup().link(),
                        PRACTICES_COLUMN, toNumberString(trainingGroup.practicesPerWeek()),
                        TRAINER_COLUMN, season.trainersFor(trainingGroup)
                            .sorted(comparing(Trainer::name))
                            .map(trainer ->
                            {
                                var factor = trainer.factorFor(trainingGroup);
                                if (factor.doubleValue() == 1.0)
                                {
                                    return trainer.link();
                                }
                                return trainer.link() + " (" + toPercentageString(factor) + ")";
                            })
                            .collect(joining(", "))
                    )
                )
                .toList()
        );
    }
}
