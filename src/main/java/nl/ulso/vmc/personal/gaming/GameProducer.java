package nl.ulso.vmc.personal.gaming;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.EntityTransformer;
import nl.ulso.curator.vault.*;

import java.util.Optional;

import static java.lang.Integer.parseInt;

@Singleton
final class GameProducer
    extends EntityTransformer<Document, Game>
{
    private static final String GAMES_FOLDER = "Games";

    @Inject
    GameProducer()
    {
    }

    @Override
    protected Class<Document> sourceClass()
    {
        return Document.class;
    }

    @Override
    protected Class<Game> targetClass()
    {
        return Game.class;
    }

    @Override
    protected Optional<Game> transform(Document document)
    {
        if (!document.folder().isRoot()
            && document.folder().parent().isRoot()
            && document.folder().name().contentEquals(GAMES_FOLDER))
        {
            var parser = new GameParser();
            document.accept(parser);
            return Optional.of(parser.game);

        }
        return Optional.empty();
    }

    private static final class GameParser
        extends BreadthFirstVaultVisitor
    {
        private String cover;
        private Integer rating;
        private Game game;

        @Override
        public void visit(Document document)
        {
            game = null;
            rating = null;
            cover = null;
            super.visit(document);
            game = new Game(document, rating, cover);
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
            cover = markdown.substring(start + 1, end + 2);
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
            try
            {
                rating = parseInt(content.substring(start + 1, end));
            }
            catch (NumberFormatException e)
            {
                // Nothing to do.
            }
        }
    }
}
