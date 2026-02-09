package nl.ulso.vmc.personal;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.*;
import nl.ulso.curator.vault.*;
import nl.ulso.date.LocalDates;

import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Comparator.naturalOrder;
import static java.util.regex.Pattern.compile;
import static nl.ulso.curator.change.Change.*;

@Singleton
final class ArticleProducer
    extends ChangeProcessorTemplate
{
    private static final String ARTICLES_FOLDER = "Articles";
    private static final String CHANGELOG_SECTION = "Changelog";
    private static final Pattern CHANGELOG_ENTRY_PATTERN =
        compile("^- \\[\\[(\\d{4}-\\d{2}-\\d{2})]]: .*$");

    private final Vault vault;

    @Inject
    ArticleProducer(Vault vault)
    {
        this.vault = vault;
    }

    @Override
    public Set<Class<?>> consumedPayloadTypes()
    {
        return Set.of(Document.class, Vault.class);
    }

    @Override
    public Set<Class<?>> producedPayloadTypes()
    {
        return Set.of(Article.class);
    }

    @Override
    protected Set<? extends ChangeHandler> createChangeHandlers()
    {
        return Set.of(
            ChangeHandler.newChangeHandler(isArticle().and(isCreate()), this::createArticle),
            ChangeHandler.newChangeHandler(isArticle().and(isUpdate()), this::updateArticle),
            ChangeHandler.newChangeHandler(isArticle().and(isDelete()), this::deleteArticle)
        );
    }

    private Predicate<Change<?>> isArticle()
    {
        return isPayloadType(Document.class).and(change ->
            {
                var folder = change.as(Document.class).value().folder();
                while (folder != vault)
                {
                    if (folder.name().equals(ARTICLES_FOLDER))
                    {
                        return true;
                    }
                    folder = folder.parent();
                }
                return false;
            }
        );
    }

    @Override
    protected void reset(ChangeCollector collector)
    {
        var folder = vault.folder(ARTICLES_FOLDER).orElse(null);
        if (folder == null)
        {
            return;
        }
        var finder = new ArticleFinder();
        folder.accept(finder);
        finder.articles.stream()
            .map(article -> create(article, Article.class))
            .forEach(collector::add);
    }

    private void createArticle(Change<?> change, ChangeCollector collector)
    {
        var articles = findArticles(change.as(Document.class));
        if (!articles.isEmpty())
        {
            collector.create(articles.getFirst(), Article.class);
        }
    }

    private void updateArticle(Change<?> change, ChangeCollector collector)
    {
        var articles = findArticles(change.as(Document.class));
        var oldArticle = articles.getFirst();
        var newArticle = articles.getLast();
        if (!oldArticle.equals(newArticle))
        {
            collector.update(oldArticle, newArticle, Article.class);
        }
    }

    private void deleteArticle(Change<?> change, ChangeCollector collector)
    {
        var articles = findArticles(change.as(Document.class));
        if (articles.isEmpty())
        {
            collector.delete(articles.getFirst(), Article.class);
        }
    }

    private List<Article> findArticles(Change<Document> change)
    {
        var finder = new ArticleFinder();
        change.values().forEach(document -> document.accept(finder));
        return finder.articles;
    }

    private static class ArticleFinder
        extends BreadthFirstVaultVisitor
    {
        private final List<Article> articles = new ArrayList<>();

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
            var date = textBlock.markdown().trim().lines()
                .map(CHANGELOG_ENTRY_PATTERN::matcher)
                .filter(Matcher::matches)
                .map(matcher -> matcher.group(1))
                .map(LocalDates::parseDateOrNull)
                .filter(Objects::nonNull)
                .max(naturalOrder());
            var article = new Article(textBlock.document().name(), date);
            articles.add(article);
        }
    }
}
