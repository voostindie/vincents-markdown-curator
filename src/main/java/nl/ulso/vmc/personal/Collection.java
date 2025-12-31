package nl.ulso.vmc.personal;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.markdown_curator.*;
import nl.ulso.markdown_curator.vault.*;
import nl.ulso.markdown_curator.vault.event.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static java.util.regex.Pattern.compile;
import static nl.ulso.markdown_curator.Changelog.emptyChangelog;

/**
 * My collection of console (PS4/PS5) games.
 */
@Singleton
public class Collection
    extends DataModelTemplate
{
    private static final String GAMES_FOLDER = "Games";
    private final Vault vault;
    private final FrontMatterUpdateCollector frontMatterUpdateCollector;
    private final Map<String, Game> games;

    @Inject
    public Collection(Vault vault, FrontMatterUpdateCollector frontMatterUpdateCollector)
    {
        this.vault = vault;
        this.frontMatterUpdateCollector = frontMatterUpdateCollector;
        this.games = new HashMap<>();
    }

    @Override
    public Changelog fullRefresh(Changelog changelog)
    {
        games.forEach(
            (name, game) -> {
                frontMatterUpdateCollector.updateFrontMatterFor(game.document(),
                    dictionary -> dictionary.removeProperty("rating")
                );
                frontMatterUpdateCollector.updateFrontMatterFor(game.document(),
                    dictionary -> dictionary.removeProperty("cover")
                );
            });
        games.clear();
        vault.folder(GAMES_FOLDER).ifPresent(folder -> folder.accept(new GameFinder()));
        return emptyChangelog();
    }

    @Override
    public Changelog process(FolderAdded event, Changelog changelog)
    {
        if (isFolderInScope(event.folder()))
        {
            return fullRefresh(changelog);
        }
        return emptyChangelog();
    }

    @Override
    public Changelog process(FolderRemoved event, Changelog changelog)
    {
        if (isFolderInScope(event.folder()))
        {
            return fullRefresh(changelog);
        }
        return emptyChangelog();
    }

    @Override
    public Changelog process(DocumentAdded event, Changelog changelog)
    {
        if (isFolderInScope(event.document().folder()))
        {
            return fullRefresh(changelog);
        }
        return emptyChangelog();
    }

    @Override
    public Changelog process(DocumentChanged event, Changelog changelog)
    {
        if (isFolderInScope(event.document().folder()))
        {
            return fullRefresh(changelog);
        }
        return emptyChangelog();
    }

    @Override
    public Changelog process(DocumentRemoved event, Changelog changelog)
    {
        if (isFolderInScope(event.document().folder()))
        {
            return fullRefresh(changelog);
        }
        return emptyChangelog();
    }

    private boolean isFolderInScope(Folder folder)
    {
        var topLevelFolderName = toplevelFolder(folder).name();
        return topLevelFolderName.contentEquals(GAMES_FOLDER);
    }

    private Folder toplevelFolder(Folder folder)
    {
        var toplevelFolder = folder;
        while (toplevelFolder != vault && toplevelFolder.parent() != vault)
        {
            toplevelFolder = toplevelFolder.parent();
        }
        return toplevelFolder;
    }

    private class GameFinder
        extends BreadthFirstVaultVisitor
    {
        private static final Pattern DATE_PATTERN = compile("\\[\\[(\\d{4}-\\d{2}-\\d{2})]]");
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
