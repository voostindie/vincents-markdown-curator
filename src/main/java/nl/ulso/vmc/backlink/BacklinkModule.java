package nl.ulso.vmc.backlink;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import nl.ulso.curator.change.ChangeProcessor;
import nl.ulso.curator.query.Query;
import nl.ulso.curator.statistics.MeasurementTracker;

/// Keeps track of backlinks to documents, but only for documents they are requested for.
///
/// Keeping all backlinks in memory all the time incurs quite a big hit on memory, which is a waste
/// if these backlinks are not actually used.
///
/// For that reason this module keeps track of backlinks only for those documents that are
/// referenced by the [BacklinkQuery] (also provided by this module).
///
/// This module requires no external configuration. Including it is enough, after which the
/// `backlinks` query becomes available.
@Module
public abstract class BacklinkModule
{
    @Binds
    @IntoSet
    abstract ChangeProcessor bindBacklinkQueryReferenceProducer(
        BacklinkQueryReferenceProducer producer);

    @Binds
    @IntoSet
    abstract ChangeProcessor bindBacklinkQueryReferenceProcessor(
        DefaultBacklinkQueryReferenceRepository repository);

    @Binds
    abstract BacklinkQueryReferenceRepository bindBacklinkQueryReferenceRepository(
        DefaultBacklinkQueryReferenceRepository repository);

    @Binds
    @IntoSet
    abstract MeasurementTracker bindBacklinkQueryReferenceTracker(
        DefaultBacklinkQueryReferenceRepository repository);

    @Binds
    @IntoSet
    abstract ChangeProcessor bindBacklinkProducer(BacklinkProducer producer);

    @Binds
    @IntoSet
    abstract ChangeProcessor bindBacklinkInitializer(BacklinkInitializer initializer);

    @Binds
    @IntoSet
    abstract ChangeProcessor bindBacklinkExpunger(BacklinkExpunger expunger);

    @Binds
    @IntoSet
    abstract ChangeProcessor bindBacklinkProcessor(DefaultBacklinkRepository repository);

    @Binds
    abstract BacklinkRepository bindBacklinkRepository(DefaultBacklinkRepository repository);

    @Binds
    @IntoSet
    abstract MeasurementTracker bindBacklinkTracker(DefaultBacklinkRepository repository);

    @Binds
    @IntoSet
    abstract Query bindBacklinksQuery(BacklinkQuery backlinkQuery);
}
