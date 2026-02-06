package nl.ulso.vmc.personal;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.*;
import nl.ulso.curator.main.*;
import nl.ulso.curator.vault.*;

import java.util.*;
import java.util.function.Predicate;

import static java.lang.Integer.parseInt;
import static java.util.Collections.emptyList;
import static nl.ulso.curator.change.Change.Kind.DELETE;
import static nl.ulso.curator.change.Change.isCreate;
import static nl.ulso.curator.change.Change.isDelete;
import static nl.ulso.curator.change.Change.isPayloadType;
import static nl.ulso.curator.change.ChangeHandler.newChangeHandler;

/// My collection of console (PS4/PS5) games.
@Singleton
public class GameCollection
    extends ChangeProcessorTemplate
{
    private static final String GAMES_FOLDER = "Games";
    private final Vault vault;
    private final FrontMatterCollector frontMatterCollector;
    private final Map<String, Game> games;

    @Inject
    public GameCollection(Vault vault, FrontMatterCollector frontMatterCollector)
    {
        this.vault = vault;
        this.frontMatterCollector = frontMatterCollector;
        this.games = new HashMap<>();
    }

    @Override
    protected Set<? extends ChangeHandler> createChangeHandlers()
    {
        return Set.of(
            newChangeHandler(isGameDocument(), this::processGameDocumentUpdate)
        );
    }

    @Override
    protected boolean isResetRequired(Changelog changelog)
    {
        return super.isResetRequired(changelog) ||
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
    public Collection<Change<?>> reset()
    {
        games.forEach(
            (_, game) -> {
                frontMatterCollector.updateFrontMatterFor(game.document(),
                    dictionary -> dictionary.removeProperty("rating")
                );
                frontMatterCollector.updateFrontMatterFor(game.document(),
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
            frontMatterCollector.updateFrontMatterFor(document, dictionary ->
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
