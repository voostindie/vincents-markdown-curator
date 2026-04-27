package nl.ulso.vmc.personal.reading;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import nl.ulso.curator.change.ChangeProcessor;
import nl.ulso.curator.query.Query;
import nl.ulso.curator.statistics.MeasurementTracker;

@Module
public abstract class ReadingModule
{
    @Binds
    abstract BookRepository bindBookRepository(DefaultBookRepository repository);

    @Binds
    @IntoSet
    abstract ChangeProcessor bindBookProcessor(DefaultBookRepository processor);

    @Binds
    @IntoSet
    abstract MeasurementTracker bindBookTracker(DefaultBookRepository tracker);

    @Binds
    @IntoSet
    abstract ChangeProcessor bindBookFrontMatterProcessor(BookFrontMatterProcessor processor);

    @Binds
    abstract AuthorRepository bindAuthorRepository(DefaultAuthorRepository repository);

    @Binds
    @IntoSet
    abstract ChangeProcessor bindAuthorProcessor(DefaultAuthorRepository processor);

    @Binds
    @IntoSet
    abstract MeasurementTracker bindAuthorTracker(DefaultAuthorRepository tracker);

    @Binds
    abstract Library bindLibrary(DefaultLibrary library);

    @Binds
    @IntoSet
    abstract Query bindBooksQuery(BooksQuery booksQuery);

    @Binds
    @IntoSet
    abstract Query bindReadingQuery(ReadingQuery readingQuery);
}
