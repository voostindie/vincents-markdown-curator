package nl.ulso.vmc.personal.reading;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.EntityFrontMatterProducer;
import nl.ulso.curator.main.FrontMatterCollector;
import nl.ulso.curator.vault.Document;
import nl.ulso.dictionary.MutableDictionary;

/// Produces custom front matter for [Book]s.
@Singleton
final class BookFrontMatterProcessor
    extends EntityFrontMatterProducer<Book>
{
    private static final String COVER_PROPERTY = "cover";
    private static final String RATING_PROPERTY = "rating";

    private final Library library;

    @Inject
    BookFrontMatterProcessor(Library library, FrontMatterCollector frontMatterCollector)
    {
        super(frontMatterCollector);
        this.library = library;
    }

    @Override
    protected Class<Book> entityClass()
    {
        return Book.class;
    }

    @Override
    protected Document resolveDocumentFrom(Book book)
    {
        return book.document();
    }

    @Override
    protected void processFrontMatter(Book book, MutableDictionary dictionary)
    {
        book.rating().ifPresentOrElse(
            rating -> dictionary.setProperty(RATING_PROPERTY, rating),
            () -> dictionary.removeProperty(RATING_PROPERTY)
        );
        book.cover().ifPresentOrElse(
            cover -> dictionary.setProperty(COVER_PROPERTY, cover),
            () -> dictionary.removeProperty(COVER_PROPERTY)
        );
        var authors = library.authorsFor(book).stream().map(Author::link).toList();
//        var authors = book.authorNames().stream().map(Author::link).toList();
        if (authors.isEmpty())
        {
            dictionary.removeProperty("authors");
        }
        else
        {
            dictionary.setProperty("authors", authors);
        }
    }

    @Override
    public String toString()
    {
        return BookFrontMatterProcessor.class.getSimpleName();
    }
}
