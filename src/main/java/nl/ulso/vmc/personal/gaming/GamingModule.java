package nl.ulso.vmc.personal.gaming;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import nl.ulso.curator.change.ChangeProcessor;
import nl.ulso.curator.statistics.MeasurementTracker;

@Module
public abstract class GamingModule
{
    @Binds
    @IntoSet
    abstract GameRepository bindGameRepository(DefaultGameRepository repository);

    @Binds
    @IntoSet
    abstract ChangeProcessor bindGameProcessor(DefaultGameRepository processor);

    @Binds
    @IntoSet
    abstract MeasurementTracker bindGameTracker(DefaultGameRepository tracker);

    @Binds
    @IntoSet
    abstract ChangeProcessor bindGameFrontMatterProcessor(GameFrontMatterProcessor processor);
}
