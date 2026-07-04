package nl.ulso.vmc.directory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.EntityTransformer;
import nl.ulso.curator.vault.Document;

import java.util.Optional;

@Singleton
final class ThirdPartyProducer
    extends EntityTransformer<Document, ThirdParty>
{
    private final String thirdPartiesFolder;

    @Inject
    ThirdPartyProducer(DirectorySettings settings)
    {
        this.thirdPartiesFolder = settings.thirdPartiesFolder();
    }

    @Override
    protected Class<Document> sourceClass()
    {
        return Document.class;
    }

    @Override
    protected Class<ThirdParty> targetClass()
    {
        return ThirdParty.class;
    }

    @Override
    protected Optional<ThirdParty> transform(Document document)
    {
        if (!document.folder().isRoot()
            && document.folder().parent().isRoot()
            && document.folder().name().contentEquals(thirdPartiesFolder))
        {
            return Optional.of(new ThirdParty(document));
        }
        return Optional.empty();
    }
}
