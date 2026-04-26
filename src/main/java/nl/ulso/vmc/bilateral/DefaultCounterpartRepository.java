package nl.ulso.vmc.bilateral;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.MapBasedEntityRepository;
import nl.ulso.curator.vault.Document;

import java.util.Collection;

@Singleton
final class DefaultCounterpartRepository
    extends MapBasedEntityRepository<Document, String, Counterpart>
    implements CounterpartRepository
{
    @Inject
    DefaultCounterpartRepository()
    {
    }

    @Override
    protected Class<Document> sourceEntityClass()
    {
        return Document.class;
    }

    @Override
    protected Class<Counterpart> targetEntityClass()
    {
        return Counterpart.class;
    }

    @Override
    public Collection<Counterpart> counterparts()
    {
        return entities();
    }

    @Override
    protected boolean isEntity(Document document)
    {
        return Counterpart.isCounterpart(document);
    }

    @Override
    protected String entityKeyFrom(Document document)
    {
        return document.name();
    }

    @Override
    protected Counterpart createEntityFrom(String name, Document document)
    {
        return new Counterpart(document);
    }
}
