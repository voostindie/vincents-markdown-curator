package nl.ulso.vmc.personal.gaming;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.SetBasedEntityRepository;

import java.util.Collection;

@Singleton
final class DefaultGameRepository
    extends SetBasedEntityRepository<Game>
    implements GameRepository
{
    @Inject
    DefaultGameRepository()
    {
    }

    @Override
    protected Class<Game> entityClass()
    {
        return Game.class;
    }

    @Override
    protected Class<?> repositoryClass()
    {
        return GameRepository.class;
    }

    @Override
    public Collection<Game> findAll()
    {
        return set();
    }
}
