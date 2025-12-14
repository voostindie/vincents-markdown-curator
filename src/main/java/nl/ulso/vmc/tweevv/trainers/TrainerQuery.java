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
public class TrainerQuery
        extends SeasonQueryTemplate
{
    @Inject
    TrainerQuery(TrainerModel trainerModel, QueryResultFactory queryResultFactory)
    {
        super(trainerModel, queryResultFactory);
    }

    @Override
    public String name()
    {
        return "trainers";
    }

    @Override
    public String description()
    {
        return "List all trainers in a season";
    }

    @Override
    protected QueryResult runFor(Season season, QueryDefinition definition)
    {
        return queryResultFactory().table(
                List.of("Trainer", "Team(s)", "Kwalificatie(s)", "Vergoeding"),
                List.of(LEFT, LEFT, LEFT, RIGHT),
                season.trainers()
                        .sorted(comparing(Trainer::name))
                        .map(trainer -> Map.of(
                                "Trainer", trainer.link(),
                                "Team(s)", trainer.assignments()
                                        .sorted(comparing(
                                                assignment -> assignment.trainingGroup().name()))
                                        .map(assignment -> {
                                            var factor = assignment.factor();
                                            if (factor.doubleValue() == 1.0)
                                            {
                                                return assignment.trainingGroup().link();
                                            }
                                            return assignment.trainingGroup().link() + " (" +
                                                   toPercentageString(factor) + ")";
                                        })
                                        .collect(joining(", ")),
                                "Kwalificatie(s)", trainer.qualifications()
                                        .map(Qualification::link)
                                        .collect(joining(", ")),
                                "Vergoeding", toEuroString(trainer.computeCompensation())))
                        .toList()
        );

    }
}
