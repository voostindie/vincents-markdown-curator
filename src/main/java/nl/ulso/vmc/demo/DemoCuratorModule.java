package nl.ulso.vmc.demo;

import dagger.Module;
import dagger.Provides;
import nl.ulso.curator.CuratorModule;
import nl.ulso.curator.journal.JournalModule;
import nl.ulso.curator.journal.JournalSettings;

import java.nio.file.Path;

import static nl.ulso.curator.VaultPaths.pathInUserHome;

@Module(includes = {CuratorModule.class, JournalModule.class})
public class DemoCuratorModule
{
    @Provides
    static Path vaultPath()
    {
        return pathInUserHome("Code", "markdown-curator-demo", "vault");
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
