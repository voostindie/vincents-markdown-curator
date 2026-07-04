package nl.ulso.vmc.personal.writing;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.SetBasedEntityRepository;

import java.util.stream.Stream;

@Singleton
final class DefaultArticleRepository
    extends SetBasedEntityRepository<Article>
    implements ArticleRepository
{
    @Inject
    DefaultArticleRepository()
    {
    }

    @Override
    protected Class<Article> entityClass()
    {
        return Article.class;
    }

    @Override
    protected Class<?> repositoryClass()
    {
        return ArticleRepository.class;
    }

    @Override
    public Stream<Article> articles()
    {
        return set().stream();
    }
}
