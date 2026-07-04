package nl.ulso.vmc.tweevv.volunteers;

import jakarta.inject.Inject;
import nl.ulso.curator.change.MapBasedEntityRepository;

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
}
