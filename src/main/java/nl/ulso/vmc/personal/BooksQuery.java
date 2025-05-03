package nl.ulso.vmc.personal;

import nl.ulso.markdown_curator.query.*;

import jakarta.inject.Inject;
import java.util.Map;

public class BooksQuery
        implements Query
{
    private final Library library;
    private final QueryResultFactory resultFactory;

    @Inject
    public BooksQuery(Library library, QueryResultFactory resultFactory)
    {
        this.library = library;
        this.resultFactory = resultFactory;
    }

    @Override
    public String name()
    {
        return "books";
    }

    @Override
    public String description()
    {
        return "Lists all books written by an author";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return Map.of("author", "Author to list books for, defaults to the current document");
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        var author = definition.configuration()
                .string("author", definition.document().name());
        return resultFactory.unorderedList(
                library.booksFor(author).stream().map(book -> book.document().link()).toList());
    }
}
