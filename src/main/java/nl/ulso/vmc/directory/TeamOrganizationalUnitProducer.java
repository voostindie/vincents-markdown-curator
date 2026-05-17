package nl.ulso.vmc.directory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.vault.Document;

@Singleton
final class TeamOrganizationalUnitProducer
    extends OrganizationalUnitProcessor<Team>
{
    @Inject
    TeamOrganizationalUnitProducer(DirectorySettings settings)
    {
        super(settings);
    }

    @Override
    protected Class<Team> entityClass()
    {
        return Team.class;
    }

    @Override
    protected Document resolveDocumentFrom(Team team)
    {
        return team.document();
    }
}
