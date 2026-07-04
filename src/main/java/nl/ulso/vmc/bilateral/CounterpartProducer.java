package nl.ulso.vmc.bilateral;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.EntityTransformer;
import nl.ulso.curator.vault.Document;

import java.util.Optional;

import static nl.ulso.vmc.bilateral.Counterpart.isCounterpart;

@Singleton
final class CounterpartProducer
    extends EntityTransformer<Document, Counterpart>
{
    @Inject
    CounterpartProducer()
    {
    }

    @Override
    protected Class<Document> sourceClass()
    {
        return Document.class;
    }

    @Override
    protected Class<Counterpart> targetClass()
    {
        return Counterpart.class;
    }

    @Override
    protected Optional<Counterpart> transform(Document document)
    {
        if (isCounterpart(document))
        {
            return Optional.of(new Counterpart(document));
        }
        return Optional.empty();
    }
}
