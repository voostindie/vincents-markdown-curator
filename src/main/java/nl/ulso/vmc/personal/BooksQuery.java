package nl.ulso.vmc.personal;

import nl.ulso.markdown_curator.query.*;

import java.util.Map;

import static nl.ulso.markdown_curator.query.QueryResult.empty;
import static nl.ulso.markdown_curator.query.QueryResult.unorderedList;

public class BooksQuery
        implements Query
{
    private final Library library;

    public BooksQuery(Library library)
    {
        this.library = library;
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
        var books = library.booksFor(author);
        if (books.isEmpty())
        {
            return empty();
        }
        return unorderedList(books.stream().map(book -> book.document().link()).toList());
    }
}