package nl.ulso.vmc.tweevv;

import com.google.inject.Provides;
import nl.ulso.markdown_curator.CuratorModule;
import nl.ulso.markdown_curator.journal.JournalModule;
import nl.ulso.markdown_curator.links.LinksModule;
import nl.ulso.vmc.hook.HooksQuery;
import nl.ulso.vmc.jxa.JxaClasspathRunner;
import nl.ulso.vmc.jxa.JxaRunner;
import nl.ulso.vmc.obsidian.StarredDocumentsQuery;
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
    private static final String JOURNAL_FOLDER = "Logboek";
    private static final String ACTIVITIES_SECTION = "Activiteiten";

    @Override
    public String name()
    {
        return "TweeVV";
    }

    @Override
    public Path vaultPath()
    {
        return iCloudIAWriterFolder("TweeVV");
    }

    @Override
    public Locale locale()
    {
        return Locale.forLanguageTag("nl");
    }

    @Override
    protected void configureCurator()
    {
        install(new JournalModule(JOURNAL_FOLDER, ACTIVITIES_SECTION));
        install(new LinksModule());
        bind(JxaRunner.class).to(JxaClasspathRunner.class);
        registerDataModel(VolunteeringModel.class);
        registerQuery(ProjectsQuery.class);
        registerQuery(OmniFocusQuery.class);
        registerQuery(VolunteersQuery.class);
        registerQuery(GroupQuery.class);
        registerQuery(HooksQuery.class);
        registerQuery(StarredDocumentsQuery.class);
    }

    @Provides
    ProjectListSettings projectListSettings()
    {
        return new ProjectListSettings(
                PROJECT_FOLDER,
                ACTIVITIES_SECTION,
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
                                  "💶 Declaraties",
                                  "✉️ Eerstvolgende nieuwsbrief").contains(name));
    }
}
