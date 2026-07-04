package nl.ulso.vmc.directory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.EntityTransformer;
import nl.ulso.curator.vault.Document;

import java.util.Optional;

@Singleton
final class TeamProducer
    extends EntityTransformer<Document, Team>
{
    private final String teamsFolder;

    @Inject
    TeamProducer(DirectorySettings settings)
    {
        this.teamsFolder = settings.teamsFolder();
    }

    @Override
    protected Class<Document> sourceClass()
    {
        return Document.class;
    }

    @Override
    protected Class<Team> targetClass()
    {
        return Team.class;
    }

    @Override
    protected Optional<Team> transform(Document document)
    {
        if (!document.folder().isRoot()
            && document.folder().parent().isRoot()
            && document.folder().name().contentEquals(teamsFolder))
        {
            return Optional.of(new Team(document));
        }
        return Optional.empty();
    }
}
