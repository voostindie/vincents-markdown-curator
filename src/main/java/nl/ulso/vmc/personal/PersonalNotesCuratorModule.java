package nl.ulso.vmc.personal;

import dagger.*;
import dagger.Module;
import dagger.multibindings.IntoSet;
import nl.ulso.markdown_curator.CuratorModule;
import nl.ulso.markdown_curator.DataModel;
import nl.ulso.markdown_curator.journal.JournalModule;
import nl.ulso.markdown_curator.journal.JournalSettings;
import nl.ulso.markdown_curator.links.LinksModule;
import nl.ulso.markdown_curator.project.*;
import nl.ulso.markdown_curator.query.Query;
import nl.ulso.vmc.hook.HooksQuery;
import nl.ulso.vmc.jxa.JxaClasspathRunner;
import nl.ulso.vmc.jxa.JxaRunner;
import nl.ulso.vmc.omnifocus.*;
import nl.ulso.vmc.rabobank.JournalLastModifiedAttributeValueResolver;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

import static java.util.Locale.ENGLISH;
import static nl.ulso.markdown_curator.VaultPaths.pathInUserHome;

@Module(includes = {
        CuratorModule.class,
        JournalModule.class,
        ProjectModule.class,
        LinksModule.class
})
abstract class PersonalNotesCuratorModule
{
    private static final String JOURNAL_FOLDER = "Journal";
    private static final String MARKER_SUB_FOLDER = "Markers";
    private static final String ACTIVITIES_SECTION = "Activities";
    private static final String PROJECT_FOLDER = "Projects";

    @Provides
    static Path vaultPath()
    {
        return pathInUserHome("Notes", "Personal");
    }

    @Provides
    static Locale locale()
    {
        return ENGLISH;
    }

    @Binds
    abstract JxaRunner bindJxaRunner(JxaClasspathRunner jxaClasspathRunner);

    @Binds
    @IntoSet
    abstract DataModel bindLibrary(Library library);

    @Binds
    @IntoSet
    abstract AttributeValueResolver<?> priorityAttributeValueResolver(
            OmniFocusPriorityAttributeValueResolver resolver);

    @Binds
    @IntoSet
    abstract AttributeValueResolver<?> statusAttributeValueResolver(
            OmniFocusStatusAttributeValueResolver resolver);

    @Binds
    @IntoSet
    abstract AttributeValueResolver<?> lastModifiedAttributeValueResolver(
            JournalLastModifiedAttributeValueResolver resolver);

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
    abstract Query bindOmniFocusQuery(OmniFocusQuery omniFocusQuery);

    @Provides
    static JournalSettings journalSettings()
    {
        return new JournalSettings(
                JOURNAL_FOLDER,
                MARKER_SUB_FOLDER,
                ACTIVITIES_SECTION,
                PROJECT_FOLDER
        );
    }

    @Provides
    static ProjectSettings projectSettings()
    {
        return new ProjectSettings(PROJECT_FOLDER);
    }

    @Provides
    static OmniFocusSettings omniFocusSettings()
    {
        return new OmniFocusSettings(PROJECT_FOLDER, "👨🏻‍💻 Personal",
                (name) -> !name.startsWith("⚡️") &&
                          !Set.of("🤖 Routine",
                                  "👨🏻‍💻 Various").contains(name));
    }
}
