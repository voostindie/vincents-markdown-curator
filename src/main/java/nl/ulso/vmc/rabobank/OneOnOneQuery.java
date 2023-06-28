package nl.ulso.vmc.rabobank;

import nl.ulso.markdown_curator.query.*;
import nl.ulso.markdown_curator.vault.*;

import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.*;

import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.WEEKS;
import static java.util.Collections.emptyMap;
import static java.util.Comparator.comparing;

class OneOnOneQuery
        implements Query
{
    private final Vault vault;
    private final QueryResultFactory resultFactory;

    @Inject
    OneOnOneQuery(Vault vault, QueryResultFactory resultFactory)
    {
        this.vault = vault;
        this.resultFactory = resultFactory;
    }

    @Override
    public String name()
    {
        return "1on1";
    }

    @Override
    public String description()
    {
        return "outputs an regular 1-on-1s";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return emptyMap();
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        return vault.folder("Contacts").map(folder ->
        {
            var finder = new OneOnOneFinder();
            folder.accept(finder);
            var contacts = finder.contacts;
            contacts.sort(comparing((Map<String, String> e) -> e.get("Date")));
            return resultFactory.table(List.of("Date", "Name", "When"), contacts);
        }).orElseGet(() -> resultFactory.error("Couldn't find the folder 'Contacts'"));
    }

    private static class OneOnOneFinder
            extends BreadthFirstVaultVisitor
    {
        private final List<Map<String, String>> contacts = new ArrayList<>();

        @Override
        public void visit(Document document)
        {
            if (!document.frontMatter().hasProperty("1-on-1"))
            {
                return;
            }
            var date = document.frontMatter().date("1-on-1", null);
            if (date != null)
            {
                contacts.add(Map.of(
                        "Date", "[[" + date + "]]",
                        "Name", document.link(),
                        "When", computeWeeksAgo(date))
                );
            }
            else
            {
                contacts.add(Map.of(
                        "Date", "",
                        "Name", document.link(),
                        "When", "Unplanned")
                );
            }
        }

        private String computeWeeksAgo(LocalDate date)
        {
            var today = LocalDate.now(systemDefault());
            if (date.isEqual(today))
            {
                return "Today";
            }
            if (date.isAfter(today))
            {
                return "In " + DAYS.between(today, date) + " day(s)";
            }
            var days = DAYS.between(date, today);
            if (days < 7)
            {
                return days + " days(s) ago";
            }
            return WEEKS.between(date, today) + " week(s) ago";
        }
    }
}