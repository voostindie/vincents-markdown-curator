package nl.ulso.vmc.tweevv.volunteers;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import nl.ulso.curator.change.ChangeProcessor;
import nl.ulso.curator.query.Query;
import nl.ulso.curator.statistics.MeasurementTracker;

@Module
public abstract class VolunteeringModule
{
    @Binds
    @IntoSet
    abstract Query bindVolunteersQuery(VolunteersQuery volunteersQuery);

    @Binds
    @IntoSet
    abstract Query bindRetiredVolunteersQuery(RetiredVolunteersQuery retiredVolunteersQuery);

    @Binds
    @IntoSet
    abstract Query bindGroupQuery(GroupQuery groupQuery);

    // TODO: remove the code below

    @Binds
    @IntoSet
    abstract ChangeProcessor bindVolunteeringModel(VolunteeringModel volunteeringModel);

    @Binds
    @IntoSet
    abstract MeasurementTracker bindVolunteeringTracker(VolunteeringModel volunteeringModel);

    //    @Binds
    //    @IntoSet
    //    abstract ChangeProcessor bindContactProducer(ContactProducer producer);
    //
    //    @Binds
    //    @IntoSet
    //    abstract ChangeProcessor bindContactProcessor(DefaultContactRepository processor);
    //
    //    @Binds
    //    @IntoSet
    //    abstract MeasurementTracker bindContactTracker(DefaultContactRepository tracker);
    //
    //    @Binds
    //    abstract ContactRepository bindContactRepository(DefaultContactRepository repository);
    //

}
