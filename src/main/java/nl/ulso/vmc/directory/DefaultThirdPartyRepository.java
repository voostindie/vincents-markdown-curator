package nl.ulso.vmc.directory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.MapBasedEntityRepository;

import java.util.Optional;

@Singleton
final class DefaultThirdPartyRepository
    extends MapBasedEntityRepository<String, ThirdParty>
    implements ThirdPartyRepository
{
    @Inject
    DefaultThirdPartyRepository()
    {
    }

    @Override
    protected Class<ThirdParty> entityClass()
    {
        return ThirdParty.class;
    }

    @Override
    protected Class<?> repositoryClass()
    {
        return ThirdPartyRepository.class;
    }

    @Override
    protected String entityKeyFrom(ThirdParty thirdParty)
    {
        return thirdParty.name();
    }

    @Override
    public Optional<ThirdParty> thirdPartyNamed(String name)
    {
        return Optional.ofNullable(map().get(name));
    }
}
