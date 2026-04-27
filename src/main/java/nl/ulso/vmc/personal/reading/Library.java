package nl.ulso.vmc.personal.reading;

import java.util.List;

public interface Library
{
    List<Book> booksFor(String author);

    List<Author> authorsFor(Book book);

    List<BookReadingSession> readingFor(int year);
}
