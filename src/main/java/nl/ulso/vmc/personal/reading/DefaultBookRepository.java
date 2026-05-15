package nl.ulso.vmc.personal.reading;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.MapBasedEntityRepository;
import nl.ulso.curator.vault.*;
import nl.ulso.date.LocalDates;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.util.regex.Pattern.compile;

@Singleton
final class DefaultBookRepository
    extends MapBasedEntityRepository<Document, String, Book>
    implements BookRepository
{
    private static final String BOOK_FOLDER = "Books";

    @Inject
    DefaultBookRepository()
    {
    }

    @Override
    public String name()
    {
        return BookRepository.class.getSimpleName();
    }

    @Override
    protected Class<Document> sourceEntityClass()
    {
        return Document.class;
    }

    @Override
    protected Class<Book> targetEntityClass()
    {
        return Book.class;
    }

    @Override
    protected boolean isEntity(Document document)
    {
        return document.folder().name().contentEquals(BOOK_FOLDER)
               && !document.folder().isRoot()
               && document.folder().parent().isRoot();
    }

    @Override
    protected String entityKeyFrom(Document document)
    {
        return document.name();
    }

    @Override
    protected Book createEntityFrom(String name, Document document)
    {
        var parser = new BookParser();
        document.accept(parser);
        return parser.book;
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

    private static class BookParser
        extends BreadthFirstVaultVisitor
    {
        private static final Pattern DATE_PATTERN = compile("\\[\\[(\\d{4}-\\d{2}-\\d{2})]]");
        private MutableBook book;

        @Override
        public void visit(Document document)
        {
            book = new MutableBook(document);
            super.visit(document);
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
                () -> extractCover(textBlock)
            );
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
                book.addAuthor(link.targetDocument())
            );
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
                    book.addReadingSession(start, Optional.ofNullable(end));
                });
        }
    }
}
