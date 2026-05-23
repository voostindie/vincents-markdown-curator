package nl.ulso.vmc.demo;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Named;
import nl.ulso.curator.CuratorModule;
import nl.ulso.curator.addon.journal.JournalSettings;
import nl.ulso.curator.addon.project.ProjectSettings;
import nl.ulso.curator.addon.projectjournal.ProjectJournalModule;

import java.nio.file.Path;

import static nl.ulso.curator.CuratorModule.WATCH_DOCUMENT_KEY;
import static nl.ulso.curator.VaultPaths.pathInUserHome;

@Module(includes = {CuratorModule.class, ProjectJournalModule.class})
public class DemoCuratorModule
{
    @Provides
    static Path vaultPath()
    {
        return pathInUserHome("Code", "markdown-curator-demo", "vault");
    }

    @Provides
    @Named(WATCH_DOCUMENT_KEY)
    static String watchDocument()
    {
        return "WATCHDOC";
    }


    @Provides
    ProjectSettings projectSettings()
    {
        return new ProjectSettings("Projects");
    }

    @Provides
    JournalSettings journalSettings()
    {
        return new JournalSettings(
            "Journal",
            "Markers",
            "Activities",
            "Projects"
        );
    }
}
