package nl.ulso.vmc.tweevv.trainers;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.query.*;

import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static nl.ulso.curator.query.QueryResultFactory.Alignment.LEFT;
import static nl.ulso.curator.query.QueryResultFactory.Alignment.RIGHT;

/// Generates a table of all qualifications in a season, including the trainers who qualify for it.
@Singleton
public class QualificationQuery
    extends SeasonQueryTemplate
{
    private static final String QUALIFICATION_COLUMN = "Kwalificatie";
    private static final String ALLOWANCE_COLUMN = "Toeslag";
    private static final String TRAINER_COLUMN = "Trainer(s)";

    @Inject
    QualificationQuery(TrainerModel trainerModel, QueryResultFactory queryResultFactory)
    {
        super(trainerModel, queryResultFactory);
    }

    @Override
    public String name()
    {
        return "qualifications";
    }

    @Override
    public String description()
    {
        return "List all qualifications in a season";
    }

    @Override
    public QueryResult runFor(Season season, QueryDefinition definition)
    {
        return queryResultFactory().table(
            List.of(QUALIFICATION_COLUMN, ALLOWANCE_COLUMN, TRAINER_COLUMN),
            List.of(LEFT, RIGHT, LEFT),
            season.qualifications()
                .sorted(comparing(Qualification::allowance).thenComparing(Qualification::name))
                .map(qualification ->
                    Map.of(
                        QUALIFICATION_COLUMN, qualification.link(),
                        ALLOWANCE_COLUMN, toEuroString(qualification.allowance()),
                        TRAINER_COLUMN, season.trainersWith(qualification)
                            .sorted(comparing(Trainer::name))
                            .map(Trainer::link)
                            .collect(joining(", "))
                    )
                )
                .toList()
        );
    }
}
