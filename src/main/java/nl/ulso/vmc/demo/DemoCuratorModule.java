package nl.ulso.vmc.demo;

import nl.ulso.markdown_curator.CuratorModule;
import nl.ulso.markdown_curator.journal.JournalModule;

import java.nio.file.Path;

public class DemoCuratorModule
        extends CuratorModule
{
    @Override
    public String name()
    {
        return "DEMO";
    }

    @Override
    public Path vaultPath()
    {
        return pathInUserHome("Code", "markdown-curator-demo", "vault");
    }

    @Override
    protected void configureCurator()
    {
        install(new JournalModule("Journal", "Activities"));
    }
}
