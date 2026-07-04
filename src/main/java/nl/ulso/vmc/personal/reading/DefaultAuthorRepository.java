package nl.ulso.vmc.personal.reading;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.MapBasedEntityRepository;

import java.util.Optional;

@Singleton
final class DefaultAuthorRepository
    extends MapBasedEntityRepository<String, Author>
    implements AuthorRepository
{
    @Inject
    DefaultAuthorRepository()
    {
    }

    @Override
    protected Class<Author> entityClass()
    {
        return Author.class;
    }

    @Override
    protected Class<?> repositoryClass()
    {
        return AuthorRepository.class;
    }

    @Override
    protected String entityKeyFrom(Author author)
    {
        return author.name();
    }

    @Override
    public Optional<Author> findByName(String name)
    {
        return Optional.ofNullable(map().get(name));
    }
}
