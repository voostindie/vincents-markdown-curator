package nl.ulso.vmc.personal;

import nl.ulso.markdown_curator.query.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;
import static nl.ulso.markdown_curator.query.QueryResult.empty;
import static nl.ulso.markdown_curator.query.QueryResult.error;
import static nl.ulso.markdown_curator.query.QueryResult.table;

public class ReadingQuery
        implements Query
{
    private final Library library;

    public ReadingQuery(Library library)
    {
        this.library = library;
    }

    @Override
    public String name()
    {
        return "reading";
    }

    @Override
    public String description()
    {
        return "Lists all sessions for a category (folder) in a specific year.";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return Map.of("year", "Year to list the sessions for");
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        var configuration = definition.configuration();
        var year = configuration.integer("year", -1);
        if (year == -1)
        {
            return error("No year specified");
        }
        var sessions = library.readingFor(year);
        if (sessions.isEmpty())
        {
            return empty();
        }
        var table = sessions.stream()
                .map(session -> Map.of(
                                "From", session.fromDate().toString(),
                                "To", session.toDate()
                                        .map(LocalDate::toString).orElse(""),
                                "Title", session.book().document().link(),
                                "Author(s)", session.book().authors().stream()
                                        .map(author -> author.document().link())
                                        .collect(joining(", ")),
                                "Rating", session.book().rating()
                                        .map(ReadingQuery::formatRating).orElse("n/a")
                        )
                )
                .toList();
        return table(List.of("Title", "Author(s)", "Rating"), table);
    }

    private static String formatRating(int rating)
    {
        return "★".repeat(rating);
    }
}
