package nl.ulso.vmc.omnifocus;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.ChangeProcessor;
import nl.ulso.curator.change.Changelog;
import nl.ulso.curator.vault.Vault;

import java.util.Set;

import static nl.ulso.curator.change.Change.isCreate;
import static nl.ulso.curator.change.Changelog.changelogFor;
import static nl.ulso.curator.change.Changelog.emptyChangelog;

/// OmniFocus projects are fetched in a background process; this processor blocks until the initial
/// fetch is complete. It is triggered only once, at application startup; that is the only time a
/// `Vault` create event is produced.
@Singleton
public class OmniFocusInitializer
    implements ChangeProcessor
{
    private final OmniFocusRepository omniFocusRepository;

    @Inject
    public OmniFocusInitializer(OmniFocusRepository omniFocusRepository)
    {
        this.omniFocusRepository = omniFocusRepository;
    }

    @Override
    public Set<Class<?>> consumedPayloadTypes()
    {
        return Set.of(Vault.class);
    }

    @Override
    public Set<Class<?>> producedPayloadTypes()
    {
        return Set.of(OmniFocusUpdate.class);
    }

    @Override
    public Changelog apply(Changelog changelog)
    {
        if (changelog.changes().anyMatch(isCreate()))
        {
            omniFocusRepository.waitForInitialFetchToComplete();
            return changelogFor(OmniFocusUpdate.OMNIFOCUS_CHANGE);
        }
        return emptyChangelog();
    }
}
