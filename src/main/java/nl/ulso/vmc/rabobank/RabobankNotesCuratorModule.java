package nl.ulso.vmc.rabobank;

import com.google.inject.Provides;
import nl.ulso.markdown_curator.CuratorModule;
import nl.ulso.markdown_curator.journal.JournalModule;
import nl.ulso.markdown_curator.links.LinksModule;
import nl.ulso.vmc.hook.HooksQuery;
import nl.ulso.vmc.jxa.JxaClasspathRunner;
import nl.ulso.vmc.jxa.JxaRunner;
import nl.ulso.vmc.omnifocus.OmniFocusQuery;
import nl.ulso.vmc.omnifocus.OmniFocusSettings;
import nl.ulso.vmc.project.ProjectListSettings;
import nl.ulso.vmc.project.ProjectsQuery;

import java.nio.file.Path;
import java.util.Set;

public class RabobankNotesCuratorModule
        extends CuratorModule
{
    private static final String PROJECT_FOLDER = "Projects";
    private static final String JOURNAL_FOLDER = "Journal";
    private static final String ACTIVITIES_SECTION = "Activities";

    @Override
    public String name()
    {
        return "Rabobank";
    }

    @Override
    public Path vaultPath()
    {
        return pathInUserHome("Notes", "Rabobank");
    }

    @Override
    protected void configureCurator()
    {
        install(new JournalModule(JOURNAL_FOLDER, ACTIVITIES_SECTION, PROJECT_FOLDER));
        install(new LinksModule());
        bind(JxaRunner.class).to(JxaClasspathRunner.class);
        registerDataModel(OrgChart.class);
        registerQuery(ProjectsQuery.class);
        registerQuery(ArticlesQuery.class);
        registerQuery(OmniFocusQuery.class);
        registerQuery(SystemsQuery.class);
        registerQuery(ArchitectureDecisionRecordsQuery.class);
        registerQuery(OneOnOneQuery.class);
        registerQuery(SubteamsQuery.class);
        registerQuery(RolesQuery.class);
        registerQuery(ChapterQuery.class);
        registerQuery(HooksQuery.class);
    }

    @Provides
    ProjectListSettings projectListSettings()
    {
        return new ProjectListSettings(PROJECT_FOLDER, ACTIVITIES_SECTION, "Last&nbsp;modified",
                "Project");
    }

    @Provides
    OmniFocusSettings omniFocusSettings()
    {
        return new OmniFocusSettings(PROJECT_FOLDER, "💼 Rabobank",
                (name) -> !name.startsWith("⚡️") &&
                          !Set.of("🤖 Routine",
                                  "🌳 Study",
                                  "💶 Statements",
                                  "💼 Various",
                                  "💬 Reminders").contains(name));
    }
}
