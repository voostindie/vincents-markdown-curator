package nl.ulso.vmc.directory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.MapBasedEntityRepository;
import nl.ulso.curator.vault.Document;

import java.util.Optional;

@Singleton
final class DefaultTeamRepository
    extends MapBasedEntityRepository<Document, String, Team>
    implements TeamRepository
{
    private final String teamFolder;

    @Inject
    DefaultTeamRepository(DirectorySettings settings)
    {
        this.teamFolder = settings.teamsFolder();
    }

    @Override
    protected Class<Document> sourceEntityClass()
    {
        return Document.class;
    }

    @Override
    protected Class<Team> targetEntityClass()
    {
        return Team.class;
    }

    @Override
    protected boolean isEntity(Document document)
    {
        return document.folder().name().contentEquals(teamFolder)
               && !document.folder().isRoot()
               && document.folder().parent().isRoot();
    }

    @Override
    protected String entityKeyFrom(Document document)
    {
        return document.name();
    }

    @Override
    protected Team createEntityFrom(String name, Document document)
    {
        return new Team(document);
    }

    @Override
    public Optional<Team> teamNamed(String name)
    {
        return Optional.ofNullable(map().get(name));
    }
}
