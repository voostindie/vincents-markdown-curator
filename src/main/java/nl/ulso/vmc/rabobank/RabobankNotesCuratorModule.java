package nl.ulso.vmc.rabobank;

import dagger.Module;
import dagger.*;
import dagger.multibindings.IntoSet;
import nl.ulso.markdown_curator.CuratorModule;
import nl.ulso.markdown_curator.DataModel;
import nl.ulso.markdown_curator.journal.JournalModule;
import nl.ulso.markdown_curator.journal.JournalSettings;
import nl.ulso.markdown_curator.links.LinksModule;
import nl.ulso.markdown_curator.query.Query;
import nl.ulso.vmc.graph.*;
import nl.ulso.vmc.hook.HooksQuery;
import nl.ulso.vmc.jxa.JxaClasspathRunner;
import nl.ulso.vmc.jxa.JxaRunner;
import nl.ulso.vmc.omnifocus.OmniFocusQuery;
import nl.ulso.vmc.omnifocus.OmniFocusSettings;
import nl.ulso.vmc.project.*;

import java.nio.file.Path;
import java.util.*;

import static java.util.Locale.ENGLISH;
import static nl.ulso.markdown_curator.VaultPaths.pathInUserHome;
import static nl.ulso.vmc.graph.Shape.HEXAGON;
import static nl.ulso.vmc.graph.Shape.RECTANGLE;
import static nl.ulso.vmc.graph.Shape.STADIUM;

@Module(includes = {
        CuratorModule.class, JournalModule.class, MermaidGraphModule.class, LinksModule.class})
abstract class RabobankNotesCuratorModule
{
    private static final String PROJECT_FOLDER = "Projects";
    private static final String MARKER_SUB_FOLDER = "Markers";
    private static final String JOURNAL_FOLDER = "Journal";
    private static final String CONTACS_FOLDER = "Contacts";
    private static final String TEAMS_FOLDER = "Teams";
    private static final String ACTIVITIES_SECTION = "Activities";

    @Provides
    static Path vaultPath()
    {
        return pathInUserHome("Notes", "Rabobank");
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
    abstract DataModel bindOrgChart(OrgChart orgChart);

    @Binds
    @IntoSet
    abstract DataModel bindProjectList(ProjectList projectList);

    @Binds
    @IntoSet
    abstract Query bindProjectListQuery(ProjectListQuery projectListQuery);

    @Binds
    @IntoSet
    abstract Query bindProjectLeadQuery(ProjectLeadQuery projectLeadQuery);

    @Binds
    @IntoSet
    abstract Query bindArticlesQuery(ArticlesQuery articlesQuery);

    @Binds
    @IntoSet
    abstract Query bindOmniFocusQuery(OmniFocusQuery omniFocusQuery);

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
    static ProjectListSettings projectListSettings()
    {
        return new ProjectListSettings(
                PROJECT_FOLDER,
                ACTIVITIES_SECTION,
                "Last&nbsp;modified",
                "Project",
                "Lead",
                "Status"
        );
    }

    @Provides
    static OmniFocusSettings omniFocusSettings()
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

    @Provides
    static MermaidGraphSettings mermaidGraphSettings()
    {
        return new MermaidGraphSettings(Set.of(
                new Type("project", PROJECT_FOLDER, RECTANGLE, new ProjectNodeClassifier()),
                new Type("contact", CONTACS_FOLDER, STADIUM),
                new Type("team", TEAMS_FOLDER, HEXAGON)
        ));
    }
}
