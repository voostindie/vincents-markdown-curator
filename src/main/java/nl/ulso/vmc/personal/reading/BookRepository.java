package nl.ulso.vmc.personal.reading;

import java.util.Collection;
import java.util.stream.Stream;

public interface BookRepository
{
    Stream<Book> byAuthor(Author author);

    Collection<Book> findAll();
}
