package nl.ulso.vmc.bilateral;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.*;
import nl.ulso.curator.vault.*;

import java.util.*;
import java.util.function.Predicate;

import static nl.ulso.curator.change.Change.*;
import static nl.ulso.curator.change.ChangeHandler.newChangeHandler;
import static nl.ulso.vmc.bilateral.Counterpart.CONTACTS_FOLDER;

@Singleton
final class DefaultCounterpartRegistry
    extends ChangeProcessorTemplate
    implements CounterpartRegistry
{
    private final Vault vault;
    private final Map<String, Counterpart> counterparts;

    @Inject
    DefaultCounterpartRegistry(Vault vault)
    {
        this.vault = vault;
        this.counterparts = new HashMap<>();
    }

    @Override
    public Set<Class<?>> consumedPayloadTypes()
    {
        return Set.of(Vault.class, Document.class);
    }

    @Override
    public Set<Class<?>> producedPayloadTypes()
    {
        return Set.of(Counterpart.class);
    }

    @Override
    protected Set<? extends ChangeHandler> createChangeHandlers()
    {
        return Set.of(
            newChangeHandler(isCreate().and(isCounterpart()), this::createCounterpart),
            newChangeHandler(isUpdate().and(isCounterpart()), this::updateCounterpart),
            newChangeHandler(isUpdate().and(isCounterpart()), this::deleteCounterpart)
        );
    }

    @Override
    protected void reset(ChangeCollector collector)
    {
        counterparts.clear();
        var finder = new CounterpartFinder();
        vault.accept(finder);
        finder.counterparts.forEach(counterpart ->
        {
            counterparts.put(counterpart.name(), counterpart);
            collector.create(counterpart, Counterpart.class);
        });
    }

    private void createCounterpart(Change<?> change, ChangeCollector collector)
    {
        var newCounterpart = new Counterpart(change.as(Document.class).newValue());
        counterparts.put(newCounterpart.name(), newCounterpart);
        collector.add(create(newCounterpart, Counterpart.class));
    }

    private void updateCounterpart(Change<?> change, ChangeCollector collector)
    {
        var newDocument = change.as(Document.class).newValue();
        var oldCounterpart = counterparts.get(newDocument.name());
        if (Counterpart.isCounterpart(newDocument))
        {
            // The new document represents a counterpart...
            if (oldCounterpart != null)
            {
                // ...and so did the old one. So, we UPDATE the counterpart.
                var newCounterpart = new Counterpart(newDocument);
                counterparts.put(newCounterpart.name(), newCounterpart);
                collector.add(update(oldCounterpart, newCounterpart, Counterpart.class));
            }
            else
            {
                // ...but the old one did not. So, we CREATE a new counterpart.
                createCounterpart(change, collector);
            }
        }
        else
        {
            // The new document is not a counterpart, but the old one was.
            // So, we DELETE the old counterpart.
            deleteCounterpart(change, collector);
        }
    }

    private void deleteCounterpart(Change<?> change, ChangeCollector collector)
    {
        var oldCounterpartName = change.as(Document.class).oldValue().name();
        var oldCounterpart = counterparts.remove(oldCounterpartName);
        collector.add(delete(oldCounterpart, Counterpart.class));
    }

    private Predicate<Change<?>> isCounterpart()
    {
        return isPayloadType(Document.class).and(
            change -> change.as(Document.class).values().anyMatch(Counterpart::isCounterpart));
    }

    @Override
    public Collection<Counterpart> counterparts()
    {
        return Collections.unmodifiableCollection(counterparts.values());
    }

    private static class CounterpartFinder
        extends BreadthFirstVaultVisitor
    {
        private final Set<Counterpart> counterparts = new HashSet<>();

        @Override
        public void visit(Vault vault)
        {
            vault.folder(CONTACTS_FOLDER).ifPresent(folder -> folder.accept(this));
        }

        @Override
        public void visit(Document document)
        {
            if (Counterpart.isCounterpart(document))
            {
                counterparts.add(new Counterpart(document));
            }
        }
    }
}
