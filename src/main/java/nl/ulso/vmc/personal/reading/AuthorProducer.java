package nl.ulso.vmc.personal.reading;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.EntityTransformer;
import nl.ulso.curator.vault.Document;

import java.util.Optional;

@Singleton
final class AuthorProducer
    extends EntityTransformer<Document, Author>
{
    private static final String AUTHOR_FOLDER = "Authors";

    @Inject
    AuthorProducer()
    {
    }

    @Override
    protected Class<Document> sourceClass()
    {
        return Document.class;
    }

    @Override
    protected Class<Author> targetClass()
    {
        return Author.class;
    }

    @Override
    protected Optional<Author> transform(Document document)
    {
        if (!document.folder().isRoot()
            && document.folder().parent().isRoot()
            && document.folder().name().contentEquals(AUTHOR_FOLDER))
        {
            return Optional.of(new Author(document));
        }
        return Optional.empty();
    }
}
