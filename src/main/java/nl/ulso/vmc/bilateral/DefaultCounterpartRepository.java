package nl.ulso.vmc.bilateral;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.ChangeCollector;
import nl.ulso.curator.change.DocumentBasedEntityRepository;
import nl.ulso.curator.vault.*;

import java.util.*;

import static nl.ulso.vmc.bilateral.Counterpart.CONTACTS_FOLDER;

@Singleton
final class DefaultCounterpartRepository
    extends DocumentBasedEntityRepository<String, Counterpart>
    implements CounterpartRepository
{
    private final Vault vault;
    // TODO: remove when the init_event_replay branch is merged
    private Map<String, Counterpart> mutableMap;

    @Inject
    DefaultCounterpartRepository(Vault vault)
    {
        this.vault = vault;
    }

    // TODO: remove when the init_event_replay branch is merged
    @Override
    protected void resetInternal(ChangeCollector collector)
    {
        var finder = new CounterpartFinder();
        vault.accept(finder);
        finder.counterparts.forEach(counterpart ->
        {
            mutableMap.put(counterpart.name(), counterpart);
            collector.create(counterpart, Counterpart.class);
        });
    }

    @Override
    protected Map<String, Counterpart> createMap()
    {
        var map = super.createMap();
        mutableMap = map;
        return map;
    }

    @Override
    public Collection<Counterpart> counterparts()
    {
        return entities();
    }

    @Override
    protected Class<Counterpart> entityClass()
    {
        return Counterpart.class;
    }

    @Override
    protected boolean isEntity(Document document)
    {
        return Counterpart.isCounterpart(document);
    }

    @Override
    protected Counterpart createEntityFrom(Document document)
    {
        return new Counterpart(document);
    }

    @Override
    protected String entityKeyFrom(Document document)
    {
        return document.name();
    }

    // TODO: remove when the init_event_replay branch is merged
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
