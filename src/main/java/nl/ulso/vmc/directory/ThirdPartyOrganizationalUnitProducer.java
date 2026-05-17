package nl.ulso.vmc.directory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.vault.Document;

@Singleton
final class ThirdPartyOrganizationalUnitProducer
    extends OrganizationalUnitProcessor<ThirdParty>
{
    @Inject
    ThirdPartyOrganizationalUnitProducer(DirectorySettings settings)
    {
        super(settings);
    }

    @Override
    protected Class<ThirdParty> entityClass()
    {
        return ThirdParty.class;
    }

    @Override
    protected Document resolveDocumentFrom(ThirdParty thirdParty)
    {
        return thirdParty.document();
    }
}
