package nl.ulso.vmc.personal;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.*;
import nl.ulso.curator.vault.*;

import java.util.*;
import java.util.function.Predicate;

import static java.lang.Integer.parseInt;
import static java.util.Collections.emptyList;
import static nl.ulso.curator.Change.Kind.DELETE;
import static nl.ulso.curator.Change.isCreate;
import static nl.ulso.curator.Change.isDelete;
import static nl.ulso.curator.Change.isPayloadType;

/**
 * My collection of console (PS4/PS5) games.
 */
@Singleton
public class GameCollection
    extends ChangeProcessorTemplate
{
    private static final String GAMES_FOLDER = "Games";
    private final Vault vault;
    private final FrontMatterUpdateCollector frontMatterUpdateCollector;
    private final Map<String, Game> games;

    @Inject
    public GameCollection(Vault vault, FrontMatterUpdateCollector frontMatterUpdateCollector)
    {
        this.vault = vault;
        this.frontMatterUpdateCollector = frontMatterUpdateCollector;
        this.games = new HashMap<>();
        this.registerChangeHandler(isGameDocument(), this::processGameDocumentUpdate);
    }

    @Override
    protected boolean isFullRefreshRequired(Changelog changelog)
    {
        return super.isFullRefreshRequired(changelog) ||
               changelog.changes().anyMatch(isGameFolder().and(isDelete().or(isCreate())));
    }

    private Predicate<Change<?>> isGameDocument()
    {
        return isPayloadType(Document.class).and(change ->
        {
            var document = (Document) change.value();
            var folder = document.folder();
            return isGameFolder(folder);
        });
    }

    private Predicate<Change<?>> isGameFolder()
    {
        return isPayloadType(Folder.class).and(change ->
        {
            var folder = (Folder) change.value();
            return isGameFolder(folder);
        });
    }

    private boolean isGameFolder(Folder folder)
    {
        return folder.name().contentEquals(GAMES_FOLDER) && folder.parent() == vault;
    }

    @Override
    public Collection<Change<?>> fullRefresh()
    {
        games.forEach(
            (_, game) -> {
                frontMatterUpdateCollector.updateFrontMatterFor(game.document(),
                    dictionary -> dictionary.removeProperty("rating")
                );
                frontMatterUpdateCollector.updateFrontMatterFor(game.document(),
                    dictionary -> dictionary.removeProperty("cover")
                );
            });
        games.clear();
        vault.folder(GAMES_FOLDER).ifPresent(folder -> folder.accept(new GameFinder()));
        return emptyList();
    }

    private Collection<Change<?>> processGameDocumentUpdate(Change<?> change)
    {
        var document = (Document) change.value();
        if (change.kind() == DELETE)
        {
            games.remove(document.name());
        }
        else
        {
            document.accept(new GameFinder());
        }
        return null;
    }

    private class GameFinder
        extends BreadthFirstVaultVisitor
    {
        private Game game;

        @Override
        public void visit(Document document)
        {
            game = new Game(document);
            games.put(document.name(), game);
            super.visit(document);
            frontMatterUpdateCollector.updateFrontMatterFor(document, dictionary ->
                {
                    game.rating().ifPresent(rating -> dictionary.setProperty("rating", rating));
                    game.cover().ifPresent(cover -> dictionary.setProperty("cover", cover));
                }
            );
        }

        @Override
        public void visit(TextBlock textBlock)
        {
            textBlock.parentSection().ifPresentOrElse(section ->
                {
                    if (section.level() != 2)
                    {
                        return;
                    }
                    if (section.title().startsWith("Rating"))
                    {
                        extractRating(textBlock);
                    }
                },
                () -> extractCover(textBlock)
            );
        }

        private void extractCover(TextBlock block)
        {
            var markdown = block.markdown();
            var start = markdown.indexOf("![[");
            if (start == -1)
            {
                return;
            }
            var end = markdown.indexOf("]]", start);
            if (end == -1)
            {
                return;
            }
            var cover = markdown.substring(start + 1, end + 2);
            game.setCover(cover);
        }

        private void extractRating(TextBlock block)
        {
            var content = block.markdown();
            var start = content.indexOf('(');
            if (start == -1)
            {
                return;
            }
            var end = content.indexOf(')', start + 1);
            if (end == -1)
            {
                return;
            }
            int rating;
            try
            {
                rating = parseInt(content.substring(start + 1, end));
            }
            catch (NumberFormatException e)
            {
                return;
            }
            game.setRating(rating);
        }
    }
}
