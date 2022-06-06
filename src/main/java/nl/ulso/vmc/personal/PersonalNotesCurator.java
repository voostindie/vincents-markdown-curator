package nl.ulso.vmc.personal;

import nl.ulso.markdown_curator.*;
import nl.ulso.markdown_curator.query.QueryCatalog;
import nl.ulso.markdown_curator.vault.FileSystemVault;
import nl.ulso.markdown_curator.vault.Vault;

import java.io.IOException;
import java.util.Set;

public class PersonalNotesCurator
        extends CuratorTemplate
{
    @Override
    protected FileSystemVault createVault()
            throws IOException
    {
        return createVaultForPathInUserHome("Notes", "Personal");
    }

    @Override
    protected Set<DataModel> createDataModels(Vault vault)
    {
        return Set.of(new Library(vault));
    }

    @Override
    protected void registerQueries(QueryCatalog catalog, Vault vault, DataModelMap dataModels)
    {
        catalog.register(new ReadingQuery(dataModels.get(Library.class)));
        catalog.register(new BooksQuery(dataModels.get(Library.class)));
    }
}
