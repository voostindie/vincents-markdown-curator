package nl.ulso.vmc.omnifocus;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.ChangeProcessorTemplate;
import nl.ulso.curator.changelog.Change;
import nl.ulso.curator.changelog.Changelog;
import nl.ulso.curator.vault.Vault;

import java.util.List;
import java.util.Set;

import static nl.ulso.curator.changelog.Change.isCreate;
import static nl.ulso.curator.changelog.Change.isPayloadType;

/// OmniFocus projects are fetched in a background process; this processor blocks until the initial
/// fetch is complete. It is triggered only once, at applications start.
@Singleton
public class OmniFocusInitializer
    extends ChangeProcessorTemplate
{
    private final OmniFocusRepository omniFocusRepository;

    @Inject
    public OmniFocusInitializer(OmniFocusRepository omniFocusRepository)
    {
        this.omniFocusRepository = omniFocusRepository;
        registerChangeHandler(isPayloadType(Vault.class).and(isCreate()), this::waitForOmniFocus);
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
    protected boolean isFullRefreshRequired(Changelog changelog)
    {
        return false;
    }

    private List<Change<?>> waitForOmniFocus(Change<?> change)
    {
        omniFocusRepository.waitForInitialFetchToComplete();
        return List.of(OmniFocusUpdate.OMNIFOCUS_CHANGE);
    }
}
