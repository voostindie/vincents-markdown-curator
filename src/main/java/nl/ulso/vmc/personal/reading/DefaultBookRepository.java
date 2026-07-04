package nl.ulso.vmc.personal.reading;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.MapBasedEntityRepository;

import java.util.Collection;
import java.util.stream.Stream;

@Singleton
final class DefaultBookRepository
    extends MapBasedEntityRepository<String, Book>
    implements BookRepository
{
    @Inject
    DefaultBookRepository()
    {
    }

    @Override
    protected Class<Book> entityClass()
    {
        return Book.class;
    }

    @Override
    protected Class<?> repositoryClass()
    {
        return BookRepository.class;
    }

    @Override
    protected String entityKeyFrom(Book book)
    {
        return book.name();
    }

    @Override
    public Stream<Book> byAuthor(Author author)
    {
        return entities().stream()
            .filter(book -> book.authorNames().contains(author.name()));
    }

    @Override
    public Collection<Book> findAll()
    {
        return entities();
    }
}
