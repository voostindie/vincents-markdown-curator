package nl.ulso.vmc.rabobank;

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
import nl.ulso.vmc.graph.*;
import nl.ulso.vmc.hook.HooksQuery;
import nl.ulso.vmc.omnifocus.OmniFocusModule;
import nl.ulso.vmc.omnifocus.OmniFocusSettings;
import nl.ulso.vmc.projectjournal.ProjectJournalModule;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

import static java.util.Locale.ENGLISH;
import static nl.ulso.markdown_curator.VaultPaths.pathInUserHome;
import static nl.ulso.vmc.graph.Shape.HEXAGON;
import static nl.ulso.vmc.graph.Shape.RECTANGLE;
import static nl.ulso.vmc.graph.Shape.STADIUM;

@Module(includes = {
        CuratorModule.class,
        ProjectModule.class,
        JournalModule.class,
        ProjectJournalModule.class,
        OmniFocusModule.class,
        MermaidGraphModule.class
})
abstract class RabobankNotesCuratorModule
{
    private static final String PROJECT_FOLDER = "Projects";
    private static final String MARKER_SUB_FOLDER = "Markers";
    private static final String JOURNAL_FOLDER = "Journal";
    private static final String CONTACS_FOLDER = "Contacts";
    private static final String TEAMS_FOLDER = "Teams";
    private static final String ACTIVITIES_SECTION = "Activities";

    @Provides
    static Path provideVaultPath()
    {
        return pathInUserHome("Notes", "Rabobank");
    }

    @Provides
    static Locale provideLocale()
    {
        return ENGLISH;
    }

    @Binds
    @IntoSet
    abstract DataModel bindOrgChart(OrgChart orgChart);

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
    abstract Query bindOneOnOneQuery(OneOnOneQuery oneOnOneQuery);

    @Binds
    @IntoSet
    abstract Query bindSubteamsQuery(SubteamsQuery subteamsQuery);

    @Binds
    @IntoSet
    abstract Query bindRolesQuery(RolesQuery rolesQuery);

    @Binds
    @IntoSet
    abstract Query bindChapterQuery(ChapterQuery chapterQuery);

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
        return new OmniFocusSettings(PROJECT_FOLDER, "💼 Rabobank",
                (name) -> !name.startsWith("⚡️") &&
                          !Set.of("🤖 Routine",
                                  "📖 Reading material",
                                  "💡 Newsletter topics",
                                  "💶 Statements",
                                  "💼 Various",
                                  "💬 Reminders").contains(name));
    }

    @Binds
    abstract MermaidNodeClassifier bindProjectNodeClassifier(
            ProjectNodeClassifier projectNodeClassifier);

    @Provides
    static MermaidGraphSettings provideMermaidGraphSettings(MermaidNodeClassifier projectNodeClassifier)
    {
        return new MermaidGraphSettings(Set.of(
                new Type("project", PROJECT_FOLDER, RECTANGLE, projectNodeClassifier),
                new Type("contact", CONTACS_FOLDER, STADIUM),
                new Type("team", TEAMS_FOLDER, HEXAGON)
        ));
    }
}
