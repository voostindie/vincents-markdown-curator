package nl.ulso.vmc.personal.gaming;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.MapBasedEntityRepository;
import nl.ulso.curator.vault.*;

import java.util.Collection;

import static java.lang.Integer.parseInt;

@Singleton
final class DefaultGameRepository
    extends MapBasedEntityRepository<Document, String, Game>
    implements GameRepository
{
    private static final String GAMES_FOLDER = "Games";

    @Inject
    DefaultGameRepository()
    {
    }

    @Override
    public String name()
    {
        return GameRepository.class.getSimpleName();
    }

    @Override
    protected Class<Document> sourceEntityClass()
    {
        return Document.class;
    }

    @Override
    protected Class<Game> targetEntityClass()
    {
        return Game.class;
    }

    @Override
    protected boolean isEntity(Document source)
    {
        return source.folder().name().contentEquals(GAMES_FOLDER)
               && !source.folder().isRoot()
               && source.folder().parent().isRoot();
    }

    @Override
    protected String entityKeyFrom(Document document)
    {
        return document.name();
    }

    @Override
    protected Game createEntityFrom(String name, Document document)
    {
        var parser = new GameParser();
        document.accept(parser);
        return parser.game;
    }

    @Override
    public Collection<Game> findAll()
    {
        return entities();
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
