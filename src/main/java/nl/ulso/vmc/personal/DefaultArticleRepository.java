package nl.ulso.vmc.personal;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.*;
import nl.ulso.curator.vault.Vault;

import java.util.*;
import java.util.stream.Stream;

import static nl.ulso.curator.change.Change.*;
import static nl.ulso.curator.change.ChangeHandler.newChangeHandler;

@Singleton
class DefaultArticleRepository
    extends ChangeProcessorTemplate
    implements ArticleRepository
{
    static final Change<?> UPDATE =
        update(new ArticleRepositoryUpdate(), ArticleRepositoryUpdate.class);

    private final Set<Article> articles;

    @Inject
    DefaultArticleRepository()
    {
        articles = new HashSet<>();
    }

    @Override
    public Set<Class<?>> consumedPayloadTypes()
    {
        return Set.of(Vault.class, Article.class);
    }

    @Override
    public Set<Class<?>> producedPayloadTypes()
    {
        return Set.of(ArticleRepositoryUpdate.class);
    }

    @Override
    protected Set<? extends ChangeHandler> createChangeHandlers()
    {
        return Set.of(
           newChangeHandler(isPayloadType(Article.class).and(isCreate()), this::articleCreated),
           newChangeHandler(isPayloadType(Article.class).and(isUpdate()), this::articleUpdated),
           newChangeHandler(isPayloadType(Article.class).and(isDelete()), this::articleDeleted)
        );
    }

    @Override
    protected void reset(ChangeCollector collector)
    {
        if (!articles.isEmpty())
        {
            articles.clear();
            collector.add(UPDATE);
        }
    }

    private void articleCreated(Change<?> change, ChangeCollector collector)
    {
        articles.add(change.as(Article.class).value());
        collector.add(UPDATE);
    }

    private void articleUpdated(Change<?> change, ChangeCollector collector)
    {
        articles.remove(change.as(Article.class).oldValue());
        articles.add(change.as(Article.class).newValue());
        collector.add(UPDATE);
    }

    private void articleDeleted(Change<?> change, ChangeCollector collector)
    {
        articles.remove(change.as(Article.class).value());
        collector.add(UPDATE);
    }

    @Override
    protected Collection<Change<?>> createChangeCollection()
    {
        return new HashSet<>(1);
    }

    @Override
    public Stream<Article> articles()
    {
        return articles.stream();
    }
}
