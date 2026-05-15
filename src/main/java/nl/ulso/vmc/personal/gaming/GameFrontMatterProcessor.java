package nl.ulso.vmc.personal.gaming;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.EntityFrontMatterProducer;
import nl.ulso.curator.main.FrontMatterCollector;
import nl.ulso.curator.vault.Document;
import nl.ulso.dictionary.MutableDictionary;

/// Produces custom front matter for [Game]s.
///
/// Whenever a [Game] is created or updated, custom front matter is set from the new object.
///
/// (There's no need to process [Game] deletions; any custom front matter in memory is cleaned up
/// automatically when the underlying document is deleted.)
@Singleton
final class GameFrontMatterProcessor
    extends EntityFrontMatterProducer<Game>
{
    private static final String COVER_PROPERTY = "cover";
    private static final String RATING_PROPERTY = "rating";

    @Inject
    GameFrontMatterProcessor(FrontMatterCollector frontMatterCollector)
    {
        super(frontMatterCollector);
    }

    @Override
    protected Class<Game> entityClass()
    {
        return Game.class;
    }

    @Override
    protected Document resolveDocumentFrom(Game game)
    {
        return game.document();
    }

    @Override
    protected void processFrontMatter(Game game, MutableDictionary dictionary)
    {
        game.rating().ifPresentOrElse(
            rating -> dictionary.setProperty(RATING_PROPERTY, rating),
            () -> dictionary.removeProperty(RATING_PROPERTY)
        );
        game.cover().ifPresentOrElse(
            cover -> dictionary.setProperty(COVER_PROPERTY, cover),
            () -> dictionary.removeProperty(COVER_PROPERTY)
        );
    }

    @Override
    public String name()
    {
        return GameFrontMatterProcessor.class.getSimpleName();
    }
}
