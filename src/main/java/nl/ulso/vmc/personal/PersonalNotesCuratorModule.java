package nl.ulso.vmc.personal;

import dagger.*;
import dagger.Module;
import dagger.multibindings.IntoSet;
import nl.ulso.markdown_curator.CuratorModule;
import nl.ulso.markdown_curator.DataModel;
import nl.ulso.markdown_curator.journal.JournalModule;
import nl.ulso.markdown_curator.journal.JournalSettings;
import nl.ulso.markdown_curator.project.ProjectModule;
import nl.ulso.markdown_curator.project.ProjectSettings;
import nl.ulso.markdown_curator.query.Query;
import nl.ulso.vmc.hook.HooksQuery;
import nl.ulso.vmc.omnifocus.OmniFocusModule;
import nl.ulso.vmc.omnifocus.OmniFocusSettings;
import nl.ulso.vmc.projectjournal.ProjectJournalModule;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

import static java.util.Locale.ENGLISH;
import static nl.ulso.markdown_curator.VaultPaths.pathInUserHome;

@Module(includes = {
        CuratorModule.class,
        ProjectModule.class,
        JournalModule.class,
        ProjectJournalModule.class,
        OmniFocusModule.class
})
abstract class PersonalNotesCuratorModule
{
    private static final String JOURNAL_FOLDER = "Journal";
    private static final String MARKER_SUB_FOLDER = "Markers";
    private static final String ACTIVITIES_SECTION = "Activities";
    private static final String PROJECT_FOLDER = "Projects";

    @Provides
    static Path provideVaultPath()
    {
        return pathInUserHome("Personal", "Notes");
    }

    @Provides
    static Locale provideLocale()
    {
        return ENGLISH;
    }

    @Binds
    @IntoSet
    abstract DataModel bindLibrary(Library library);

    @Binds
    @IntoSet
    abstract Query bindReadingQuery(ReadingQuery readingQuery);

    @Binds
    @IntoSet
    abstract Query bindBooksQuery(BooksQuery booksQuery);

    @Binds
    @IntoSet
    abstract Query bindHooksQuery(HooksQuery HooksQuery);

    @Binds
    @IntoSet
    abstract DataModel bindGameCollection(GameCollection gameCollection);

    @Provides
    static JournalSettings provideJournalSettings()
    {
        return new JournalSettings(
                JOURNAL_FOLDER,
                MARKER_SUB_FOLDER,
                ACTIVITIES_SECTION,
                PROJECT_FOLDER
        );
    }

    @Provides
    static ProjectSettings provideProjectSettings()
    {
        return new ProjectSettings(PROJECT_FOLDER);
    }

    @Provides
    static OmniFocusSettings provideOmniFocusSettings()
    {
        return new OmniFocusSettings(PROJECT_FOLDER, "👨🏻‍💻 Personal",
                (name) -> !name.startsWith("⚡️") &&
                          !Set.of("🤖 Routine",
                                  "👨🏻‍💻 Various").contains(name));
    }
}
