package nl.ulso.vmc.directory;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import nl.ulso.curator.change.ChangeProcessor;
import nl.ulso.curator.query.Query;
import nl.ulso.curator.statistics.MeasurementTracker;

@Module
public abstract class DirectoryModule
{
    @Binds
    abstract ContactRepository bindContactRepository(DefaultContactRepository repository);

    @Binds
    @IntoSet
    abstract ChangeProcessor bindContactProcessor(DefaultContactRepository processor);

    @Binds
    @IntoSet
    abstract MeasurementTracker bindContactTracker(DefaultContactRepository tracker);

    @Binds
    abstract TeamRepository bindTeamRepository(DefaultTeamRepository repository);

    @Binds
    @IntoSet
    abstract ChangeProcessor bindTeamProcessor(DefaultTeamRepository processor);

    @Binds
    @IntoSet
    abstract MeasurementTracker bindTeamTracker(DefaultTeamRepository tracker);

    @Binds
    abstract ThirdPartyRepository bindThirdPartyRepository(DefaultThirdPartyRepository repository);

    @Binds
    @IntoSet
    abstract ChangeProcessor bindThirdPartyProcessor(DefaultThirdPartyRepository processor);

    @Binds
    @IntoSet
    abstract MeasurementTracker bindThirdPartyTracker(DefaultThirdPartyRepository tracker);

    @Binds
    @IntoSet
    abstract ChangeProcessor bindTeamOrganizationalUnitProducer(
        TeamOrganizationalUnitProducer producer);

    @Binds
    @IntoSet
    abstract ChangeProcessor bindThirdPartyOrganizationalUnitProducer(
        ThirdPartyOrganizationalUnitProducer producer);

    @Binds
    abstract Directory bindDirectory(DefaultDirectory directory);

    @Binds
    @IntoSet
    abstract ChangeProcessor bindDirectoryProcessor(DefaultDirectory processor);

    @Binds
    @IntoSet
    abstract MeasurementTracker bindDirectoryTracker(DefaultDirectory tracker);

    @Binds
    @IntoSet
    abstract Query bindRolesQuery(RolesQuery query);

    @Binds
    @IntoSet
    abstract Query bindSubteamsQuery(DepartmentsQuery query);
}
