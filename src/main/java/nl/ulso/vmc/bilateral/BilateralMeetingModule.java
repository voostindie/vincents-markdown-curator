package nl.ulso.vmc.bilateral;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import nl.ulso.curator.change.ChangeProcessor;
import nl.ulso.curator.query.Query;
import nl.ulso.curator.statistics.MeasurementTracker;

/// Module that tracks bilateral meetings with specific contacts - counterparts - in the journal.
///
/// [Counterpart]s are contacts with a specific marking; see [CounterpartRepository]. Bilateral
/// meetings are meetings with [Counterpart]s in the [Journal] that are recognized by a special
/// pattern; see [BilateralMeetingRepository].
@Module
public abstract class BilateralMeetingModule
{
    @Binds
    @IntoSet
    abstract ChangeProcessor bindCounterpartProcessor(
        DefaultCounterpartRepository counterpartRegistry);

    @Binds
    @IntoSet
    abstract ChangeProcessor bindBilateralMeetingProcessor(
        DefaultBilateralMeetingRepository bilateralMeetingRegistry);

    @Binds
    abstract CounterpartRepository bindAttendeeRegistry(
        DefaultCounterpartRepository counterpartRegistry);

    @Binds
    abstract BilateralMeetingRepository bindBilateralMeetingRegistry(
        DefaultBilateralMeetingRepository bilateralMeetingRegistry);

    @Binds
    @IntoSet
    abstract Query bindBilateralMeetingQuery(BilateralMeetingQuery bilateralMeetingQuery);

    @Binds
    @IntoSet
    abstract MeasurementTracker bindMeasurementTracker(
        DefaultCounterpartRepository counterpartRegistry);
}
