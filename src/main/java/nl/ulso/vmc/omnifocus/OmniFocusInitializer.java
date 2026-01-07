package nl.ulso.vmc.omnifocus;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.markdown_curator.*;
import nl.ulso.markdown_curator.vault.Vault;

import java.util.List;
import java.util.Set;

import static nl.ulso.markdown_curator.Change.isCreate;
import static nl.ulso.markdown_curator.Change.isObjectType;

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
        registerChangeHandler(isObjectType(Vault.class).and(isCreate()), this::waitForOmniFocus);
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
        return false;
    }

    private List<Change<?>> waitForOmniFocus(Change<?> change)
    {
        omniFocusRepository.waitForInitialFetchToComplete();
        return List.of(OmniFocusUpdate.OMNIFOCUS_CHANGE);
    }
}
