package nl.ulso.vmc.personal;

import nl.ulso.markdown_curator.DataModelTemplate;
import nl.ulso.markdown_curator.vault.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static java.time.LocalDate.now;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.regex.Pattern.compile;

/**
 * This is the start of what will hopefully be a richer data model at some point. For example
 * I'd like to keep track of book series as well, and list them in order.
 */
public class Library
        extends DataModelTemplate
{
    private final Vault vault;
    private final Map<String, Author> authors;
    private final Map<String, Book> books;
    private final Set<ReadingSession> readingSessions;

    public Library(Vault vault)
    {
        this.vault = vault;
        this.authors = new HashMap<>();
        this.books = new HashMap<>();
        this.readingSessions = new HashSet<>();
    }

    @Override
    protected void fullRefresh()
    {
        authors.clear();
        books.clear();
        readingSessions.clear();
        vault.folder("Authors").ifPresent(folder -> folder.accept(new AuthorFinder()));
        vault.folder("Books").ifPresent(folder -> folder.accept(new BookFinder()));
    }

    List<ReadingSession> readingFor(int year)
    {
        var stillReading = now();
        return readingSessions.stream()
                .filter(session -> session.fromDate().getYear() == year)
                .sorted(comparing(ReadingSession::fromDate)
                        .thenComparing(s -> s.toDate().orElse(stillReading))
                        .thenComparing(s -> s.book().name()))
                .toList();
    }

    List<Book> booksFor(String authorName)
    {
        var author = authors.get(authorName);
        if (author == null)
        {
            return emptyList();
        }
        return books.values().stream()
                .filter(book -> book.authors().contains(author))
                .sorted(comparing(Book::name))
                .toList();
    }

    private class AuthorFinder
            extends BreadthFirstVaultVisitor
    {
        @Override
        public void visit(Document document)
        {
            var author = new Author(document);
            authors.put(author.name(), author);
        }
    }

    private class BookFinder
            extends BreadthFirstVaultVisitor
    {
        private static final Pattern DATE_PATTERN = compile("\\[\\[(\\d{4}-\\d{2}-\\d{2})]]");
        private Book book;

        @Override
        public void visit(Document document)
        {
            book = new Book(document);
            books.put(book.name(), book);
            super.visit(document);
        }

        @Override
        public void visit(TextBlock textBlock)
        {
            textBlock.parentSection().ifPresent(section ->
            {
                if (section.level() != 2)
                {
                    return;
                }
                if (section.title().startsWith("Author"))
                {
                    extractAuthors(textBlock);
                }
                if (section.title().startsWith("Rating"))
                {
                    extractRating(textBlock);
                }
                if (section.title().startsWith("Reading"))
                {
                    extractSessions(textBlock);
                }
            });
        }

        private void extractAuthors(TextBlock block)
        {
            block.findInternalLinks().forEach(link -> {
                var author = authors.get(link.targetDocument());
                if (author != null)
                {
                    book.addAuthor(author);
                }
            });
        }

        private void extractRating(TextBlock block)
        {
            var content = block.content();
            var start = content.indexOf('(');
            if (start == -1)
            {
                return;
            }
            var end = content.indexOf(')', start + 1);
            if (end == -1)
            {
                return;
            }
            int rating;
            try
            {
                rating = parseInt(content.substring(start + 1, end));
            }
            catch (NumberFormatException e)
            {
                return;
            }
            book.setRating(rating);
        }

        private void extractSessions(TextBlock block)
        {
            block.lines().stream()
                    .filter(line -> line.startsWith("- [["))
                    .map(DATE_PATTERN::matcher)
                    .map(Matcher::results)
                    .forEach(stream -> {
                        var dates = stream
                                .map(match -> match.group(1))
                                .limit(2)
                                .map(dateString -> {
                                    try
                                    {
                                        return LocalDate.parse(dateString);
                                    }
                                    catch (DateTimeParseException e)
                                    {
                                        return null;
                                    }
                                })
                                .filter(Objects::nonNull)
                                .toList();
                        if (dates.isEmpty())
                        {
                            return;
                        }
                        var start = dates.get(0);
                        LocalDate end = null;
                        if (dates.size() > 1)
                        {
                            end = dates.get(1);
                        }
                        readingSessions.add(
                                new ReadingSession(start, Optional.ofNullable(end), book));
                    });
        }
    }
}