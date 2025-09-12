package nl.ulso.vmc.personal;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.markdown_curator.DataModelTemplate;
import nl.ulso.markdown_curator.FrontMatterUpdateCollector;
import nl.ulso.markdown_curator.vault.*;
import nl.ulso.markdown_curator.vault.event.*;

import java.time.LocalDate;
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
@Singleton
public class Library
        extends DataModelTemplate
{
    private static final String AUTHORS_FOLDER = "Authors";
    private static final String BOOKS_FOLDER = "Books";
    private final Vault vault;
    private final Map<String, Author> authors;
    private final Map<String, Book> books;
    private final Set<ReadingSession> readingSessions;
    private final FrontMatterUpdateCollector frontMatterUpdateCollector;

    @Inject
    public Library(Vault vault, FrontMatterUpdateCollector frontMatterUpdateCollector)
    {
        this.vault = vault;
        this.frontMatterUpdateCollector = frontMatterUpdateCollector;
        this.authors = new HashMap<>();
        this.books = new HashMap<>();
        this.readingSessions = new HashSet<>();
    }

    @Override
    public void fullRefresh()
    {
        authors.clear();
        books.forEach(
                (name, book) -> frontMatterUpdateCollector.updateFrontMatterFor(book.document(),
                        dictionary -> dictionary.removeProperty("rating")));
        books.clear();
        readingSessions.clear();
        vault.folder(AUTHORS_FOLDER).ifPresent(folder -> folder.accept(new AuthorFinder()));
        vault.folder(BOOKS_FOLDER).ifPresent(folder -> folder.accept(new BookFinder()));
    }

    @Override
    public void process(FolderAdded event)
    {
        if (isFolderInScope(event.folder()))
        {
            fullRefresh();
        }
    }

    @Override
    public void process(FolderRemoved event)
    {
        if (isFolderInScope(event.folder()))
        {
            fullRefresh();
        }
    }

    @Override
    public void process(DocumentAdded event)
    {
        if (isFolderInScope(event.document().folder()))
        {
            fullRefresh();
        }
    }

    @Override
    public void process(DocumentChanged event)
    {
        if (isFolderInScope(event.document().folder()))
        {
            fullRefresh();
        }
    }

    @Override
    public void process(DocumentRemoved event)
    {
        if (isFolderInScope(event.document().folder()))
        {
            fullRefresh();
        }
    }

    private boolean isFolderInScope(Folder folder)
    {
        var topLevelFolderName = toplevelFolder(folder).name();
        return topLevelFolderName.contentEquals(AUTHORS_FOLDER) ||
               topLevelFolderName.contentEquals(BOOKS_FOLDER);
    }

    private Folder toplevelFolder(Folder folder)
    {
        var toplevelFolder = folder;
        while (toplevelFolder != vault && toplevelFolder.parent() != vault)
        {
            toplevelFolder = toplevelFolder.parent();
        }
        return toplevelFolder;
    }

    List<ReadingSession> readingFor(int year)
    {
        var stillReading = now();
        return readingSessions.stream()
                .filter(session -> session.fromDate().getYear() <= year
                                   && session.toDate().map(date -> date.getYear() >= year)
                                           .orElse(true))
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
            frontMatterUpdateCollector.updateFrontMatterFor(document, dictionary ->
            {
                book.rating().ifPresent(rating -> dictionary.setProperty("rating", rating));
                book.cover().ifPresent(cover -> dictionary.setProperty("cover", cover));
                var authors = book.authors().stream().map(Author::link).toList();
                dictionary.setProperty("authors", authors);
            });
        }

        @Override
        public void visit(TextBlock textBlock)
        {
            textBlock.parentSection().ifPresentOrElse(section ->
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
                    },
                    () -> extractCover(textBlock));
        }

        private void extractCover(TextBlock block)
        {
            var markdown = block.markdown();
            var start = markdown.indexOf("![[");
            if (start == -1)
            {
                return;
            }
            var end = markdown.indexOf("]]", start);
            if (end == -1)
            {
                return;
            }
            var cover = markdown.substring(start + 1, end + 2);
            book.setCover(cover);
        }

        private void extractAuthors(TextBlock block)
        {
            block.findInternalLinks().forEach(link ->
            {
                var author = authors.get(link.targetDocument());
                if (author != null)
                {
                    book.addAuthor(author);
                }
            });
        }

        private void extractRating(TextBlock block)
        {
            var content = block.markdown();
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
            block.markdown().lines()
                    .filter(line -> line.startsWith("- [["))
                    .map(DATE_PATTERN::matcher)
                    .map(Matcher::results)
                    .forEach(stream ->
                    {
                        var dates = stream
                                .map(match -> match.group(1))
                                .limit(2)
                                .map(LocalDates::parseDateOrNull)
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