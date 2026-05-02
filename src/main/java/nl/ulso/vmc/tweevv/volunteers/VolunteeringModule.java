package nl.ulso.vmc.tweevv.volunteers;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import nl.ulso.curator.change.ChangeProcessor;
import nl.ulso.curator.query.Query;

@Module
public abstract class VolunteeringModule
{
    @Binds
    @IntoSet
    abstract ChangeProcessor bindVolunteeringModel(VolunteeringModel volunteeringModel);

    @Binds
    @IntoSet
    abstract Query bindVolunteersQuery(VolunteersQuery volunteersQuery);

    @Binds
    @IntoSet
    abstract Query bindRetiredVolunteersQuery(RetiredVolunteersQuery retiredVolunteersQuery);

    @Binds
    @IntoSet
    abstract Query bindGroupQuery(GroupQuery groupQuery);


}
