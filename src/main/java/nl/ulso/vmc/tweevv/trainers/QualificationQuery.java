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
public class QualificationQuery
        extends SeasonQueryTemplate
{
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
                List.of("Kwalificatie", "Toeslag", "Trainer(s)"),
                List.of(LEFT, RIGHT, LEFT),
                season.qualifications()
                        .sorted(comparing(Qualification::allowance)
                                .thenComparing(Qualification::name))
                        .map(qualification -> Map.of(
                                "Kwalificatie", qualification.link(),
                                "Toeslag", toEuroString(qualification.allowance()),
                                "Trainer(s)", season.trainersWith(qualification)
                                        .sorted(comparing(Trainer::name))
                                        .map(Trainer::link)
                                        .collect(joining(", "))
                        ))
                        .toList()
        );
    }
}
