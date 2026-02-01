package nl.ulso.vmc.personal;

import jakarta.inject.Inject;
import nl.ulso.curator.changelog.Changelog;
import nl.ulso.curator.query.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

public class ReadingQuery
    implements Query
{
    private final Library library;
    private final QueryResultFactory resultFactory;

    @Inject
    public ReadingQuery(Library library, QueryResultFactory resultFactory)
    {
        this.library = library;
        this.resultFactory = resultFactory;
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
    public boolean isImpactedBy(Changelog changelog, QueryDefinition definition)
    {
        return changelog.changes().anyMatch(
            library.isBookDocument().or(library.isAuthorDocument())
        );
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        var configuration = definition.configuration();
        var year = configuration.integer("year", -1);
        if (year == -1)
        {
            return resultFactory.error("No year specified");
        }
        var sessions = library.readingFor(year);
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
        return resultFactory.table(List.of("Title", "Author(s)", "Rating"), table);
    }

    private static String formatRating(int rating)
    {
        return "★".repeat(rating);
    }
}
