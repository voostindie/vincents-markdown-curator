package nl.ulso.vmc.personal.reading;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.ChangeCollector;
import nl.ulso.curator.change.EntityProcessor;

import java.util.*;

/// Whenever a book is created, updated, or deleted, this processor ensures that an update event for
/// every existing author in the system is published, so that the [BooksQuery] picks it up.
@Singleton
final class BookAuthorProcessor
    extends EntityProcessor<Book>
{
    private final AuthorRepository authorRepository;

    @Inject
    BookAuthorProcessor(AuthorRepository authorRepository)
    {
        this.authorRepository = authorRepository;
    }

    @Override
    public Set<Class<?>> producedPayloadTypes()
    {
        return Set.of(Author.class);
    }

    @Override
    protected Class<Book> entityClass()
    {
        return Book.class;
    }

    @Override
    protected void entityCreated(Book newBook, ChangeCollector collector)
    {
        updateExistingBookAuthors(newBook.authorNames(), collector);
    }

    @Override
    protected void entityUpdate(Book oldBook, Book newBook, ChangeCollector collector)
    {
        var oldAuthors = new HashSet<>(oldBook.authorNames());
        var newAuthors = new HashSet<>(newBook.authorNames());
        var symmetricDiff = new HashSet<>(oldAuthors);
        symmetricDiff.addAll(newAuthors);
        oldAuthors.retainAll(newAuthors);
        symmetricDiff.removeAll(oldAuthors);
        updateExistingBookAuthors(symmetricDiff, collector);
    }

    @Override
    protected void entityDeleted(Book oldBook, ChangeCollector collector)
    {
        updateExistingBookAuthors(oldBook.authorNames(), collector);
    }

    private void updateExistingBookAuthors(Collection<String> authorNames, ChangeCollector collector)
    {
        authorNames.stream().map(authorRepository::findByName)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(author -> collector.update(author, Author.class));
    }

    @Override
    public String toString()
    {
        return BookAuthorProcessor.class.getSimpleName();
    }
}
