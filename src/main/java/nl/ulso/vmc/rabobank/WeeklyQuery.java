package nl.ulso.vmc.rabobank;

import nl.ulso.markdown_curator.query.*;
import nl.ulso.markdown_curator.vault.Document;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;

import static java.lang.System.lineSeparator;
import static java.time.format.TextStyle.FULL;
import static java.util.Comparator.comparing;

class WeeklyQuery
        implements Query
{
    private final Journal journal;

    @Inject
    WeeklyQuery(Journal journal)
    {
        this.journal = journal;
    }

    @Override
    public String name()
    {
        return "weekly";
    }

    @Override
    public String description()
    {
        return "outputs an overview of activities in a certain week";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return Map.of("year", "Year of the weekly. Default: parsed from the team name.",
                "week", "Week of the weekly. Default: parsed from the team name.");
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        var date = LocalDate.now();
        var year = definition.configuration().integer("year", date.getYear());
        var week = definition.configuration()
                .integer("week", date.get(WeekFields.of(Locale.getDefault()).weekOfYear()));
        return new Weekly(journal.forWeek(year, week));
    }

    private static class Weekly
            implements QueryResult
    {
        private final Map<String, Map<Document, List<JournalEntry>>> entries;

        public Weekly(Map<String, Map<Document, List<JournalEntry>>> entries)
        {
            this.entries = entries;
        }

        @Override
        public String toMarkdown()
        {
            if (entries.isEmpty())
            {
                return "No entries found for this week";
            }
            var builder = new StringBuilder();
            var total = 0;
            for (String folder : Journal.FOLDERS_IN_ORDER)
            {
                var map = entries.get(folder);
                if (map == null)
                {
                    continue;
                }
                builder.append("## ")
                        .append(folder)
                        .append(lineSeparator())
                        .append(lineSeparator());
                var documents =
                        map.keySet().stream()
                                .sorted(comparing(Document::sortableTitle))
                                .toList();
                for (Document document : documents)
                {
                    builder.append("- **")
                            .append(document.link())
                            .append("**")
                            .append(lineSeparator());
                    for (JournalEntry entry : map.get(document))
                    {
                        builder.append("    - *")
                                .append(entry.date().getDayOfWeek()
                                        .getDisplayName(FULL, Locale.getDefault()))
                                .append("*: [[")
                                .append(entry.section().document().name())
                                .append("#")
                                .append(entry.section().anchor())
                                .append("|")
                                .append(entry.subject())
                                .append("]]")
                                .append(lineSeparator());
                        total++;
                    }
                }
                builder.append(lineSeparator());
            }
            builder.append("(*").append(total).append(" journal entries*)");
            return builder.toString();
        }
    }
}
