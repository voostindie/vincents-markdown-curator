package nl.ulso.vmc.personal.reading;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.MapBasedEntityRepository;
import nl.ulso.curator.vault.Document;

import java.util.Optional;

@Singleton
final class DefaultAuthorRepository
    extends MapBasedEntityRepository<Document, String, Author>
    implements AuthorRepository
{
    private static final String AUTHOR_FOLDER = "Authors";

    @Inject
    DefaultAuthorRepository()
    {
    }

    @Override
    public String name()
    {
        return AuthorRepository.class.getSimpleName();
    }

    @Override
    protected Class<Document> sourceEntityClass()
    {
        return Document.class;
    }

    @Override
    protected Class<Author> targetEntityClass()
    {
        return Author.class;
    }

    @Override
    protected boolean isEntity(Document document)
    {
        return document.folder().name().contentEquals(AUTHOR_FOLDER)
               && !document.folder().isRoot()
               && document.folder().parent().isRoot();
    }

    @Override
    protected String entityKeyFrom(Document document)
    {
        return document.name();
    }

    @Override
    protected Author createEntityFrom(String name, Document document)
    {
        return new Author(document);
    }

    @Override
    public Optional<Author> findByName(String name)
    {
        return Optional.ofNullable(map().get(name));
    }
}
