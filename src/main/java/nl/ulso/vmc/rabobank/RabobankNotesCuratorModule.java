package nl.ulso.vmc.rabobank;

import dagger.*;
import dagger.Module;
import dagger.multibindings.IntoSet;
import jakarta.inject.Named;
import nl.ulso.curator.CuratorModule;
import nl.ulso.curator.addon.journal.JournalModule;
import nl.ulso.curator.addon.journal.JournalSettings;
import nl.ulso.curator.addon.omnifocus.OmniFocusModule;
import nl.ulso.curator.addon.omnifocus.OmniFocusSettings;
import nl.ulso.curator.addon.project.ProjectModule;
import nl.ulso.curator.addon.project.ProjectSettings;
import nl.ulso.curator.addon.projectjournal.ProjectJournalModule;
import nl.ulso.curator.query.Query;
import nl.ulso.vmc.bilateral.BilateralMeetingModule;
import nl.ulso.vmc.directory.DirectoryModule;
import nl.ulso.vmc.directory.DirectorySettings;
import nl.ulso.vmc.graph.*;
import nl.ulso.vmc.hook.HooksQuery;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

import static java.util.Locale.ENGLISH;
import static nl.ulso.curator.CuratorModule.WATCH_DOCUMENT_KEY;
import static nl.ulso.curator.VaultPaths.pathInUserHome;
import static nl.ulso.vmc.graph.Shape.HEXAGON;
import static nl.ulso.vmc.graph.Shape.RECTANGLE;
import static nl.ulso.vmc.graph.Shape.STADIUM;

@Module(includes = {
    CuratorModule.class,
    ProjectModule.class,
    JournalModule.class,
    ProjectJournalModule.class,
    OmniFocusModule.class,
    BilateralMeetingModule.class,
    DirectoryModule.class,
    MermaidGraphModule.class
})
abstract class RabobankNotesCuratorModule
{
    private static final String PROJECT_FOLDER = "Projects";
    private static final String MARKER_SUB_FOLDER = "Markers";
    private static final String JOURNAL_FOLDER = "Journal";
    private static final String CONTACTS_FOLDER = "Contacts";
    private static final String TEAMS_FOLDER = "Teams";
    private static final String THIRD_PARTIES_FOLDER = "3rd Parties";
    private static final String ACTIVITIES_SECTION = "Activities";
    private static final String CONTACTS_SECTION = "Contacts";

    @Provides
    static Path provideVaultPath()
    {
        return pathInUserHome("Rabobank", "Notes");
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
        return "WATCHDOC";
    }

    @Binds
    @IntoSet
    abstract Query bindArticlesQuery(ArticlesQuery articlesQuery);

    @Binds
    @IntoSet
    abstract Query bindSystemsQuery(SystemsQuery systemsQuery);

    @Binds
    @IntoSet
    abstract Query bindArchitectureDecisionRecordsQuery(
        ArchitectureDecisionRecordsQuery architectureDecisionRecordsQuery);

    @Binds
    @IntoSet
    abstract Query bindHooksQuery(HooksQuery systemsQuery);

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
        return new OmniFocusSettings("💼 Rabobank",
            (name) -> !name.startsWith("⚡️") &&
                      !Set.of("🤖 Routine",
                          "📖 Reading material",
                          "💡 Newsletter topics",
                          "💶 Statements",
                          "💼 Various",
                          "💬 Reminders"
                      ).contains(name)
        );
    }

    @Provides
    static DirectorySettings provideOrganizationSettings()
    {
        return new DirectorySettings(
            TEAMS_FOLDER,
            CONTACTS_FOLDER,
            THIRD_PARTIES_FOLDER,
            CONTACTS_SECTION
        );
    }

    @Binds
    abstract MermaidNodeClassifier bindProjectNodeClassifier(
        ProjectNodeClassifier projectNodeClassifier);

    @Provides
    static MermaidGraphSettings provideMermaidGraphSettings(
        MermaidNodeClassifier projectNodeClassifier)
    {
        return new MermaidGraphSettings(Set.of(
            new Type("project", PROJECT_FOLDER, RECTANGLE, projectNodeClassifier),
            new Type("contact", CONTACTS_FOLDER, STADIUM),
            new Type("team", TEAMS_FOLDER, HEXAGON)
        ));
    }
}
