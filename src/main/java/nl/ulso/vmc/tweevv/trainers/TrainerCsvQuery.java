package nl.ulso.vmc.tweevv.trainers;

import de.siegmar.fastcsv.writer.CsvWriter;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.markdown_curator.query.*;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.List;

import static de.siegmar.fastcsv.writer.LineDelimiter.PLATFORM;
import static java.lang.System.lineSeparator;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;

/**
 * Generates CSV data for trainers in a season; the CSV contains everything needed to later generate
 * and send out trainer agreements (by the secretary), pay out trainer compensations (by the
 * treasurer), and do the yearly verification of the certificates of conduct (by the general board
 * member).
 * <p/>
 * The CSV is generated as query output and therefore ends up in the Markdown document. That's the
 * easiest way to do it; it's what the Markdown Curator is created for. Writing CSV to a separate
 * file sounds easy enough, but is actually difficult to do efficiently. Any existing CSV file
 * should only be overwritten if the contents changed. How do we know? Within Markdown content, this
 * is exactly what the curator takes care of. And copy-pasting the CSV to a separate file, or into
 * Numbers or Excel, is easy enough as it is.
 */
@Singleton
public class TrainerCsvQuery
    extends SeasonQueryTemplate
{
    private static final String TRAINER_COLUMN = "Trainer";
    private static final String EMAIL_COLUMN = "E-mail";
    private static final String RESIDENCY_COLUMN = "Woonplaats";
    private static final String IBAN_COLUMN = "IBAN";
    private static final String COC_COLUMN = "VOG";
    private static final String UNDER_16_COLUMN = "Onder 16";
    private static final String COACH_COLUMN = "Coach";
    private static final String TEAM_COLUMN = "Teams";
    private static final String QUALIFICATION_COLUMN = "Kwalificaties";
    private static final String COMPENSATION_COLUMN = "Vergoeding";

    @Inject
    TrainerCsvQuery(TrainerModel trainerModel, QueryResultFactory queryResultFactory)
    {
        super(trainerModel, queryResultFactory);
    }

    @Override
    public String name()
    {
        return "trainerscsv";
    }

    @Override
    public String description()
    {
        return "List all trainers in a season in CSV format";
    }

    @Override
    protected QueryResult runFor(Season season, QueryDefinition definition)
    {
        try (var stringWriter = new StringWriter();
             var csvWriter = CsvWriter.builder().lineDelimiter(PLATFORM).build(stringWriter))
        {
            stringWriter.write("```csv");
            stringWriter.write(lineSeparator());
            csvWriter.writeRecord(
                List.of(TRAINER_COLUMN, EMAIL_COLUMN, RESIDENCY_COLUMN, IBAN_COLUMN,
                    COC_COLUMN, UNDER_16_COLUMN, COACH_COLUMN, TEAM_COLUMN,
                    QUALIFICATION_COLUMN,
                    COMPENSATION_COLUMN));
            season.trainers().sorted(comparing(Trainer::name)).forEach(trainer ->
                csvWriter.writeRecord(
                    trainer.name(),
                    trainer.email().orElse(null),
                    trainer.residency().orElse(null),
                    trainer.iban().orElse(null),
                    trainer.certificateOfConductDate().map(LocalDate::toString)
                        .orElse(null),
                    trainer.isUnder16() ? "true" : "false",
                    trainer.isCoach() ? "true" : "false",
                    trainer.assignments()
                        .sorted(comparing(
                            assignment -> assignment.trainingGroup().name()))
                        .map(assignment -> {
                            var factor = assignment.factor();
                            if (factor.doubleValue() == 1.0)
                            {
                                return assignment.trainingGroup().name();
                            }
                            return assignment.trainingGroup().name() + " (" +
                                   toPercentageString(factor) + ")";
                        })
                        .collect(joining(", ")),
                    trainer.qualifications()
                        .map(Qualification::name)
                        .collect(joining(", ")),
                    toEuroString(trainer.computeCompensation())
                ));
            csvWriter.flush();
            stringWriter.write("```");
            stringWriter.write(lineSeparator());
            return queryResultFactory().string(stringWriter.toString());
        }
        catch (IOException e)
        {
            // This won't fail, as the underlying writer is string; there's no I/O.
            throw new IllegalStateException("Unexpected IOException while writing to a string", e);
        }
    }
}
