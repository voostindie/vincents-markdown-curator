package nl.ulso.vmc.directory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.MapBasedEntityRepository;
import nl.ulso.curator.vault.Document;

import java.util.Optional;

@Singleton
final class DefaultThirdPartyRepository
    extends MapBasedEntityRepository<Document, String, ThirdParty>
    implements ThirdPartyRepository
{
    private final String thirdPartiesFolder;

    @Inject
    DefaultThirdPartyRepository(DirectorySettings settings)
    {
        this.thirdPartiesFolder = settings.thirdPartiesFolder();
    }

    @Override
    protected Class<Document> sourceEntityClass()
    {
        return Document.class;
    }

    @Override
    protected Class<ThirdParty> targetEntityClass()
    {
        return ThirdParty.class;
    }

    @Override
    protected boolean isEntity(Document document)
    {
        return document.folder().name().contentEquals(thirdPartiesFolder)
               && !document.folder().isRoot()
               && document.folder().parent().isRoot();
    }

    @Override
    protected String entityKeyFrom(Document document)
    {
        return document.name();
    }

    @Override
    protected ThirdParty createEntityFrom(String name, Document document)
    {
        return new ThirdParty(document);
    }

    @Override
    public Optional<ThirdParty> thirdPartyNamed(String name)
    {
        return Optional.ofNullable(map().get(name));
    }
}
