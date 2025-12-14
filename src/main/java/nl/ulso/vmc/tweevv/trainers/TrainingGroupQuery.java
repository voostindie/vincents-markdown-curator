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
public class TrainingGroupQuery
        extends SeasonQueryTemplate
{
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
                List.of("Trainingsgroep", "Tariefgroep", "Trainingen", "Trainer(s)"),
                List.of(LEFT, LEFT, RIGHT, LEFT),
                season.trainingGroups()
                        .sorted(comparing(TrainingGroup::name))
                        .map(trainingGroup -> Map.of(
                                "Trainingsgroep", trainingGroup.link(),
                                "Tariefgroep", trainingGroup.tariffGroup().link(),
                                "Trainingen", toNumberString(trainingGroup.practicesPerWeek()),
                                "Trainer(s)", season.trainersFor(trainingGroup)
                                        .sorted(comparing(Trainer::name))
                                        .map(trainer -> {
                                            var factor = trainer.factorFor(trainingGroup);
                                            if (factor.doubleValue() == 1.0)
                                            {
                                                return trainer.link();
                                            }
                                            return trainer.link() + " (" + toPercentageString(factor) + ")";
                                        })
                                        .collect(joining(", "))
                        ))
                        .toList()
        );
    }
}
