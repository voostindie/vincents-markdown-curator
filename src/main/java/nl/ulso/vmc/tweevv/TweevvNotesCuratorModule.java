package nl.ulso.vmc.tweevv;

import com.google.inject.Provides;
import nl.ulso.markdown_curator.CuratorModule;
import nl.ulso.vmc.omnifocus.OmniFocusQuery;
import nl.ulso.vmc.omnifocus.OmniFocusSettings;
import nl.ulso.vmc.project.ProjectListSettings;
import nl.ulso.vmc.project.ProjectsQuery;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

public class TweevvNotesCuratorModule
        extends CuratorModule
{
    private static final String PROJECT_FOLDER = "Projecten";

    @Override
    public String name()
    {
        return "TweeVV";
    }

    @Override
    public Path vaultPath()
    {
        return pathInUserHome("Notes", "TweeVV");
    }

    @Override
    public Locale locale()
    {
        return Locale.forLanguageTag("nl");
    }

    @Override
    protected void configureCurator()
    {
        registerDataModel(VolunteeringModel.class);
        registerQuery(ProjectsQuery.class);
        registerQuery(OmniFocusQuery.class);
        registerQuery(VolunteersQuery.class);
        registerQuery(GroupQuery.class);
    }

    @Provides
    ProjectListSettings projectListSettings()
    {
        return new ProjectListSettings(
                PROJECT_FOLDER,
                "Activiteiten",
                "Datum",
                "Project");
    }

    @Provides
    OmniFocusSettings omniFocusSettings()
    {
        return new OmniFocusSettings(
                PROJECT_FOLDER,
                "🏐 TweeVV",
                (name) -> !name.startsWith("⚡️") &&
                          !Set.of("🤖 Routine",
                                  "🏐 Diversen",
                                  "💶 Declaraties").contains(name));
    }
}
