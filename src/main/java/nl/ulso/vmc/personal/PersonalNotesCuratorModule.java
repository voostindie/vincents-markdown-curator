package nl.ulso.vmc.personal;

import dagger.*;
import dagger.Module;
import dagger.multibindings.IntoSet;
import jakarta.inject.Named;
import nl.ulso.curator.CuratorModule;
import nl.ulso.curator.changelog.ChangeProcessor;
import nl.ulso.curator.addon.journal.JournalModule;
import nl.ulso.curator.addon.journal.JournalSettings;
import nl.ulso.curator.addon.project.ProjectModule;
import nl.ulso.curator.addon.project.ProjectSettings;
import nl.ulso.curator.query.Query;
import nl.ulso.vmc.hook.HooksQuery;
import nl.ulso.vmc.omnifocus.OmniFocusModule;
import nl.ulso.vmc.omnifocus.OmniFocusSettings;
import nl.ulso.vmc.projectjournal.ProjectJournalModule;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

import static java.util.Locale.ENGLISH;
import static nl.ulso.curator.VaultPaths.pathInUserHome;
import static nl.ulso.curator.CuratorModule.WATCH_DOCUMENT_KEY;

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

    @Provides
    @Named(WATCH_DOCUMENT_KEY)
    static String watchDocument()
    {
        return "README";
    }

    @Binds
    @IntoSet
    abstract ChangeProcessor bindLibrary(Library library);

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
    abstract ChangeProcessor bindGameCollection(GameCollection gameCollection);

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
                          "👨🏻‍💻 Various"
                      ).contains(name)
        );
    }
}
