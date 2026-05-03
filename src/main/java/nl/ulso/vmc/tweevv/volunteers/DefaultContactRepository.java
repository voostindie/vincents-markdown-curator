package nl.ulso.vmc.tweevv.volunteers;

import jakarta.inject.Inject;
import nl.ulso.curator.change.MapBasedEntityRepository;
import nl.ulso.curator.vault.Document;

final class DefaultContactRepository
    extends MapBasedEntityRepository<Document, String, Contact>
    implements ContactRepository
{
    private static final String CONTACT_FOLDER = "Contacten";

    @Inject
    DefaultContactRepository()
    {
    }

    @Override
    protected Class<Document> sourceEntityClass()
    {
        return Document.class;
    }

    @Override
    protected Class<Contact> targetEntityClass()
    {
        return Contact.class;
    }

    @Override
    protected boolean isEntity(Document document)
    {
        return document.folder().name().contentEquals(CONTACT_FOLDER)
               && document.folder().parent().isRoot();
    }

    @Override
    protected String entityKeyFrom(Document document)
    {
        return document.name();
    }

    @Override
    protected Contact createEntityFrom(String name, Document document)
    {
        return new Contact(document);
    }
}
