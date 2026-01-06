package nl.ulso.vmc.omnifocus;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.markdown_curator.*;
import nl.ulso.markdown_curator.vault.Vault;

import java.util.*;

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
    }

    @Override
    public Set<Class<?>> consumedObjectTypes()
    {
        return Set.of(Vault.class);
    }

    @Override
    public Set<Class<?>> producedObjectTypes()
    {
        return Set.of(OmniFocusUpdate.class);
    }

    @Override
    protected boolean isFullRefreshRequired(Changelog changelog)
    {
        return true;
    }

    @Override
    protected Collection<Change<?>> fullRefresh()
    {
        omniFocusRepository.waitForInitialFetchToComplete();
        return List.of(OmniFocusUpdate.OMNIFOCUS_CHANGE);
    }
}
