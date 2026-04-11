package nl.ulso.vmc.bilateral;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.Changelog;
import nl.ulso.curator.query.*;

import java.time.LocalDate;
import java.util.*;

import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.WEEKS;
import static java.util.Collections.emptyMap;
import static nl.ulso.vmc.bilateral.BilateralMeetingRegistry.FALLBACK_NEVER;

@Singleton
final class BilateralMeetingQuery
    implements Query
{
    private static final String OVERDUE_PREFIX = "<span style=\"color:red\">**";
    private static final String OVERDUE_POSTFIX = "**</span>";
    private static final String COLUMN_DATE = "Date";
    private static final String COLUMN_COUNTERPART = "Name";
    private static final String COLUMN_WHEN = "When";
    private static final String COLUMN_RECURRENCE = "Recurrence";

    private final BilateralMeetingRegistry bilateralMeetingRegistry;
    private final QueryResultFactory queryResultFactory;

    @Inject
    BilateralMeetingQuery(BilateralMeetingRegistry bilateralMeetingRegistry, QueryResultFactory queryResultFactory)
    {
        this.bilateralMeetingRegistry = bilateralMeetingRegistry;
        this.queryResultFactory = queryResultFactory;
    }

    @Override
    public String name()
    {
        return "bilateral";
    }

    @Override
    public String description()
    {
        return "Outputs a table of all tracked bilateral meetings, ordered on date, newest first.";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return emptyMap();
    }

    @Override
    public boolean isImpactedBy(Changelog changelog, QueryDefinition definition)
    {
        return changelog.changesFor(BilateralRegistryUpdate.class).findFirst().isPresent();
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        var meetings = bilateralMeetingRegistry.resolveBilateralMeetings();
        var table = new ArrayList<Map<String, String>>(meetings.size());
        meetings.forEach((counterpart, date) ->
            table.add(Map.of(
                COLUMN_DATE, date.equals(FALLBACK_NEVER) ? "N/A" : "[[" + date + "]]",
                COLUMN_COUNTERPART, counterpart.link(),
                COLUMN_WHEN, computeTimeAgo(date, counterpart.recurrenceInDays()),
                COLUMN_RECURRENCE, computeRecurrence(counterpart.recurrenceInDays())
            )));
        return table.isEmpty()
               ? queryResultFactory.empty()
               : queryResultFactory.table(
                   List.of(COLUMN_DATE, COLUMN_COUNTERPART, COLUMN_WHEN, COLUMN_RECURRENCE), table);
    }

    private String computeTimeAgo(LocalDate date, int recurrenceInDays)
    {
        if (date.equals(FALLBACK_NEVER))
        {
            return "N/A";
        }
        var today = LocalDate.now(systemDefault());
        if (date.isEqual(today))
        {
            return "Today";
        }
        if (date.isAfter(today))
        {
            return "In " + DAYS.between(today, date) + " day(s)";
        }
        var builder = new StringBuilder();
        var days = DAYS.between(date, today);
        if (days > recurrenceInDays)
        {
            builder.append(OVERDUE_PREFIX);
        }
        if (days < 7)
        {
            var multiplier = days == 1 ? "" : "s";
            builder.append(days).append(" day").append(multiplier).append(" ago");
        }
        else
        {
            var weeks = WEEKS.between(date, today);
            var multiplier = weeks == 1 ? "" : "s";
            builder.append(weeks).append(" week").append(multiplier).append(" ago");
        }
        if (days > recurrenceInDays)
        {
            builder.append(OVERDUE_POSTFIX);
        }
        return builder.toString();
    }

    private String computeRecurrence(int recurrenceInDays)
    {
        int weeks = recurrenceInDays / 7;
        int days = recurrenceInDays % 7;
        var builder = new StringBuilder();
        builder.append("Every ");
        if (weeks > 0)
        {
            builder.append(weeks).append(" week");
            if (weeks != 1)
            {
                builder.append("s");
            }
        }
        if (weeks > 0 && days > 0)
        {
            builder.append(", ");
        }
        if (days > 0)
        {
            builder.append(days).append(" day");
            if (days != 1)
            {
                builder.append("s");
            }
        }
        return builder.toString();
    }
}
