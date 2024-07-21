package nl.ulso.vmc.personal;

import com.google.inject.Provides;
import nl.ulso.markdown_curator.CuratorModule;
import nl.ulso.markdown_curator.journal.JournalModule;
import nl.ulso.markdown_curator.links.LinksModule;
import nl.ulso.vmc.hook.HooksQuery;
import nl.ulso.vmc.jxa.JxaClasspathRunner;
import nl.ulso.vmc.jxa.JxaRunner;
import nl.ulso.vmc.omnifocus.OmniFocusQuery;
import nl.ulso.vmc.omnifocus.OmniFocusSettings;
import nl.ulso.vmc.project.*;

import java.nio.file.Path;
import java.util.Set;

public class PersonalNotesCuratorModule
        extends CuratorModule
{
    private static final String JOURNAL_FOLDER = "Journal";
    private static final String MARKER_SUB_FOLDER = "Markers";
    private static final String ACTIVITIES_SECTION = "Activities";
    private static final String PROJECT_FOLDER = "Projects";

    @Override
    public String name()
    {
        return "Personal";
    }

    @Override
    public Path vaultPath()
    {
        return iCloudObsidianVault("Personal");
    }

    @Override
    protected void configureCurator()
    {
        install(new JournalModule(JOURNAL_FOLDER, MARKER_SUB_FOLDER, ACTIVITIES_SECTION,
                PROJECT_FOLDER));
        install(new LinksModule());
        bind(JxaRunner.class).to(JxaClasspathRunner.class);
        registerDataModel(Library.class);
        registerDataModel(ProjectList.class);
        registerQuery(ReadingQuery.class);
        registerQuery(BooksQuery.class);
        registerQuery(HooksQuery.class);
        registerQuery(ProjectListQuery.class);
        registerQuery(OmniFocusQuery.class);
    }

    @Provides
    ProjectListSettings projectListSettings()
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
    OmniFocusSettings omniFocusSettings()
    {
        return new OmniFocusSettings(PROJECT_FOLDER, "👨🏻‍💻 Personal",
                (name) -> !name.startsWith("⚡️") &&
                          !Set.of("🤖 Routine",
                                  "👨🏻‍💻 Various").contains(name));
    }
}
