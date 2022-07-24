package nl.ulso.vmc.rabobank;

import com.google.inject.Provides;
import nl.ulso.markdown_curator.CuratorModule;
import nl.ulso.vmc.omnifocus.OmniFocusQuery;
import nl.ulso.vmc.omnifocus.OmniFocusSettings;
import nl.ulso.vmc.project.ProjectsQuery;
import nl.ulso.vmc.project.ProjectListSettings;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import static java.util.Locale.ENGLISH;

public class RabobankNotesCuratorModule
        extends CuratorModule
{
    private static final String PROJECT_FOLDER = "Projects";

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
        registerDataModel(Journal.class);
        registerDataModel(OrgChart.class);
        registerQuery(ProjectsQuery.class);
        registerQuery(ArticlesQuery.class);
        registerQuery(OmniFocusQuery.class);
        registerQuery(SystemsQuery.class);
        registerQuery(ArchitectureDecisionRecordsQuery.class);
        registerQuery(OneOnOneQuery.class);
        registerQuery(WeeklyQuery.class);
        registerQuery(SubteamsQuery.class);
        registerQuery(RolesQuery.class);
    }

    @Provides
    ProjectListSettings projectListSettings()
    {
        return new ProjectListSettings(
                PROJECT_FOLDER,
                "Activities",
                "Date",
                "Project");
    }

    @Provides
    OmniFocusSettings omniFocusSettings()
    {
        return new OmniFocusSettings(
                PROJECT_FOLDER,
                "💼 Rabobank",
                List.of(
                        "🤖 Routine",
                        "👮🏼‍♂️ STEP PDA",
                        "🌳 Study",
                        "🌳 GROW!",
                        "💶 Statements",
                        "💼 Various",
                        "🧠 Reminders"
                ));
    }

    @Provides
    Locale locale()
    {
        return ENGLISH;
    }
}
