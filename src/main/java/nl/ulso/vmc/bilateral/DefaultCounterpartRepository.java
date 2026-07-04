package nl.ulso.vmc.bilateral;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.MapBasedEntityRepository;

import java.util.Collection;

@Singleton
final class DefaultCounterpartRepository
    extends MapBasedEntityRepository<String, Counterpart>
    implements CounterpartRepository
{
    @Inject
    DefaultCounterpartRepository()
    {
    }

    @Override
    protected Class<Counterpart> entityClass()
    {
        return Counterpart.class;
    }

    @Override
    protected Class<?> repositoryClass()
    {
        return CounterpartRepository.class;
    }

    @Override
    protected String entityKeyFrom(Counterpart counterpart)
    {
        return counterpart.name();
    }

    @Override
    public Collection<Counterpart> counterparts()
    {
        return entities();
    }
}
