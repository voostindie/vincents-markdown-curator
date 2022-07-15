package nl.ulso.vmc.personal;

import nl.ulso.markdown_curator.*;
import nl.ulso.markdown_curator.query.QueryCatalog;
import nl.ulso.markdown_curator.vault.FileSystemVault;
import nl.ulso.markdown_curator.vault.Vault;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public class PersonalNotesCuratorModule
        extends CuratorModule
{
    @Override
    public String name()
    {
        return "Personal";
    }

    @Override
    public Path vaultPath()
    {
        return pathInUserHome("Notes", "Personal");
    }

    @Override
    protected void configureCurator()
    {
        registerDataModel(Library.class);
        registerQuery(ReadingQuery.class);
        registerQuery(BooksQuery.class);
    }
}
