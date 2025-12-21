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
 * Generates a concise table of all trainers in a season, including their qualifications and the
 * teams they train.
 * <p/>
 * This table is really meant for display purposes, as a quick lookup, For a query that generates
 * detailed information per trainer, see the {@link TrainerCsvQuery}.
 */
@Singleton
public class TrainerQuery
    extends SeasonQueryTemplate
{
    private static final String TRAINER_COLUMN       = "Trainer";
    private static final String TEAM_COLUMN          = "Team(s)";
    private static final String QUALIFICATION_COLUMN = "Kwalificatie(s)";
    private static final String COMPENSATION_COLUMN  = "Vergoeding";

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
            List.of(TRAINER_COLUMN, TEAM_COLUMN, QUALIFICATION_COLUMN, COMPENSATION_COLUMN),
            List.of(LEFT, LEFT, LEFT, RIGHT),
            season.trainers()
                .sorted(comparing(Trainer::name))
                .map(trainer -> Map.of(
                    TRAINER_COLUMN, trainer.link(),
                    TEAM_COLUMN, trainer.assignments()
                        .sorted(comparing(assignment -> assignment.trainingGroup().name()))
                        .map(assignment ->
                        {
                            var factor = assignment.factor();
                            if (factor.doubleValue() == 1.0)
                            {
                                return assignment.trainingGroup().link();
                            }
                            return assignment.trainingGroup().link() + " (" +
                                   toPercentageString(factor) + ")";
                        })
                        .collect(joining(", ")),
                    QUALIFICATION_COLUMN, trainer.qualifications()
                        .map(Qualification::link)
                        .collect(joining(", ")),
                    COMPENSATION_COLUMN, toEuroString(trainer.computeCompensation())
                ))
                .toList()
        );

    }
}
