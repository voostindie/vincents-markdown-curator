package nl.ulso.vmc.directory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.MapBasedEntityRepository;
import nl.ulso.curator.vault.Document;

import java.util.Optional;

@Singleton
final class DefaultContactRepository
    extends MapBasedEntityRepository<Document, String, Contact>
    implements ContactRepository
{
    private final String contactFolder;

    @Inject
    DefaultContactRepository(DirectorySettings settings)
    {
        this.contactFolder = settings.contactsFolder();
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
        return document.folder().name().contentEquals(contactFolder)
               && !document.folder().isRoot()
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

    @Override
    public Optional<Contact> contactNamed(String name)
    {
        return Optional.ofNullable(map().get(name));
    }
}
