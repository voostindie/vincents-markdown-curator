package nl.ulso.vmc.personal.writing;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.MapBasedEntityRepository;
import nl.ulso.curator.vault.*;
import nl.ulso.date.LocalDates;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.Comparator.naturalOrder;
import static java.util.regex.Pattern.compile;

@Singleton
final class DefaultArticleRepository
    extends MapBasedEntityRepository<Document, String, Article>
    implements ArticleRepository
{
    private static final String ARTICLES_FOLDER = "Articles";
    private static final String CHANGELOG_SECTION = "Changelog";
    private static final Pattern CHANGELOG_ENTRY_PATTERN =
        compile("^- \\[\\[(\\d{4}-\\d{2}-\\d{2})]]: .*$");

    @Inject
    DefaultArticleRepository()
    {
    }

    @Override
    protected Class<Document> sourceEntityClass()
    {
        return Document.class;
    }

    @Override
    protected Class<Article> targetEntityClass()
    {
        return Article.class;
    }

    @Override
    protected boolean isEntity(Document document)
    {
        return document.isInPath(ARTICLES_FOLDER);
    }

    @Override
    protected Article createEntityFrom(String name, Document document)
    {
        var dateFinder = new DateFinder();
        document.accept(dateFinder);
        return new Article(document.title(), dateFinder.date);
    }

    @Override
    protected String entityKeyFrom(Document document)
    {
        return document.name();
    }

    @Override
    public Stream<Article> articles()
    {
        return entities().stream();
    }

    private static class DateFinder
        extends BreadthFirstVaultVisitor
    {
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private Optional<LocalDate> date;

        @Override
        public void visit(Section section)
        {
            if (section.level() == 2
                && section.sortableTitle().contentEquals(CHANGELOG_SECTION)
                && !section.fragments().isEmpty()
                && section.fragments().getFirst() instanceof TextBlock textBlock)
            {
                textBlock.accept(this);
            }
        }

        @Override
        public void visit(TextBlock textBlock)
        {
            this.date = textBlock.markdown().trim().lines()
                .map(CHANGELOG_ENTRY_PATTERN::matcher)
                .filter(Matcher::matches)
                .map(matcher -> matcher.group(1))
                .map(LocalDates::parseDateOrNull)
                .filter(Objects::nonNull)
                .max(naturalOrder());
        }
    }
}
