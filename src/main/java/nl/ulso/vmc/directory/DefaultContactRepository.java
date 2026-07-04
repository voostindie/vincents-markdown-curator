package nl.ulso.vmc.directory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.MapBasedEntityRepository;

import java.util.Optional;

@Singleton
final class DefaultContactRepository
    extends MapBasedEntityRepository<String, Contact>
    implements ContactRepository
{
    @Inject
    DefaultContactRepository()
    {
    }

    @Override
    protected Class<Contact> entityClass()
    {
        return Contact.class;
    }

    @Override
    protected Class<?> repositoryClass()
    {
        return ContactRepository.class;
    }

    @Override
    protected String entityKeyFrom(Contact contact)
    {
        return contact.name();
    }

    @Override
    public Optional<Contact> contactNamed(String name)
    {
        return Optional.ofNullable(map().get(name));
    }
}
