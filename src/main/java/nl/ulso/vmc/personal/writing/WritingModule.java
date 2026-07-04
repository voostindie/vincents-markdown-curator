package nl.ulso.vmc.personal.writing;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import nl.ulso.curator.change.ChangeProcessor;
import nl.ulso.curator.query.Query;
import nl.ulso.curator.statistics.MeasurementTracker;

@Module
public abstract class WritingModule
{
    @Binds
    @IntoSet
    abstract ChangeProcessor bindArticleProducer(ArticleProducer producer);

    @Binds
    abstract ArticleRepository bindArticleRepository(DefaultArticleRepository repository);

    @Binds
    @IntoSet
    abstract ChangeProcessor bindArticleProcessor(DefaultArticleRepository processor);

    @Binds
    @IntoSet
    abstract MeasurementTracker bindArticleMTracker(DefaultArticleRepository tracker);

    @Binds
    @IntoSet
    abstract Query bindArticleQuery(ArticleQuery articleQuery);
}
