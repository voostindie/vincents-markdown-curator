package nl.ulso.vmc.personal.reading;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Optional;

import static java.time.LocalDate.now;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;

@Singleton
final class DefaultLibrary
    implements Library
{
    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    @Inject
    DefaultLibrary(AuthorRepository authorRepository, BookRepository bookRepository)
    {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    public List<Book> booksFor(String authorName)
    {
        return authorRepository.findByName(authorName).map(author ->
            bookRepository.byAuthor(author)
                .sorted(comparing(Book::name))
                .toList()
        ).orElse(emptyList());
    }

    @Override
    public List<Author> authorsFor(Book book)
    {
        return book.authorNames().stream()
            .map(authorRepository::findByName)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
    }

    @Override
    public List<BookReadingSession> readingFor(int year)
    {
        var stillReading = now();
        return bookRepository.findAll().stream()
            .flatMap(book -> book.readingSessions().stream()
                .filter(session -> session.fromDate().getYear() <= year
                                   && session.toDate().map(date -> date.getYear() >= year)
                                       .orElse(true))
                .map(session -> new BookReadingSession(book, session)))
            .sorted(comparing(BookReadingSession::fromDate)
                .thenComparing(s -> s.toDate().orElse(stillReading))
                .thenComparing(s -> s.book().name()))
            .toList();
    }
}
