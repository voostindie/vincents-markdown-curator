package nl.ulso.vmc.directory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.EntityTransformer;
import nl.ulso.curator.vault.Document;

import java.util.Optional;

@Singleton
final class ContactProducer
    extends EntityTransformer<Document, Contact>
{
    private final String contactsFolder;

    @Inject
    ContactProducer(DirectorySettings settings)
    {
        this.contactsFolder = settings.contactsFolder();
    }

    @Override
    protected Class<Document> sourceClass()
    {
        return Document.class;
    }

    @Override
    protected Class<Contact> targetClass()
    {
        return Contact.class;
    }

    @Override
    protected Optional<Contact> transform(Document document)
    {
        if (!document.folder().isRoot()
            && document.folder().parent().isRoot()
            && document.folder().name().contentEquals(contactsFolder))
        {
            return Optional.of(new Contact(document));
        }
        return Optional.empty();
    }
}
