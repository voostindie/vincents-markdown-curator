package nl.ulso.vmc.rabobank;

import nl.ulso.markdown_curator.journal.Journal;
import nl.ulso.markdown_curator.query.QueryDefinition;
import nl.ulso.markdown_curator.query.QueryResultFactory;

import javax.inject.Inject;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Map;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static java.time.LocalDate.now;
import static java.time.temporal.ChronoField.DAY_OF_WEEK;
import static java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR;
import static java.util.regex.Pattern.compile;

public class WeeklyQuery
        extends PeriodQuery
{
    private static final Pattern WEEKLY_DOCUMENT_NAME_PATTERN = compile("^(\\d{4}) Week (\\d{2})$");
    private static final int FIRST_DAY_OF_THE_WEEK = 1;
    private static final int INVALID = -1;
    private static final int ONE_WEEK = 7;

    @Inject
    WeeklyQuery(Journal model, QueryResultFactory resultFactory)
    {
        super(model, resultFactory);
    }

    @Override
    public String name()
    {
        return "weekly";
    }

    @Override
    public String description()
    {
        return "Generates a weekly overview of activities, extracted from the journal";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return Map.of(
                "year", "Year of the overview; defaults to the year from the document name",
                "week", "Week of the overview; defaults to the week from the document name",
                "folder", "Folder of notes to report on; defaults to '" + DEFAULT_FOLDER + "'"
        );
    }

    @Override
    protected LocalDate resolveStartDate(QueryDefinition definition)
    {
        var year = definition.configuration().integer("year", INVALID);
        var week = definition.configuration().integer("week", INVALID);
        if (year == INVALID || week == INVALID)
        {
            var matcher = WEEKLY_DOCUMENT_NAME_PATTERN.matcher(definition.document().name());
            if (matcher.matches())
            {
                year = year != INVALID ? year : parseInt(matcher.group(1));
                week = week != INVALID ? week : parseInt(matcher.group(2));
            }
            else
            {
                return null;
            }
        }
        try
        {
            return now()
                    .withYear(year)
                    .with(WEEK_OF_WEEK_BASED_YEAR, week)
                    .with(DAY_OF_WEEK, FIRST_DAY_OF_THE_WEEK);
        }
        catch (DateTimeException e)
        {
            return null;
        }
    }

    @Override
    protected int resolveDuration(QueryDefinition definition)
    {
        return ONE_WEEK;
    }
}
