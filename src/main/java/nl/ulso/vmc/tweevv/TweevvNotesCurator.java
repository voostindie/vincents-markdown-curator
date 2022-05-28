package nl.ulso.vmc.tweevv;

import nl.ulso.markdown_curator.*;
import nl.ulso.markdown_curator.query.QueryCatalog;
import nl.ulso.markdown_curator.vault.FileSystemVault;
import nl.ulso.markdown_curator.vault.Vault;
import nl.ulso.vmc.omnifocus.OmniFocusQuery;
import nl.ulso.vmc.project.ProjectListQuery;
import nl.ulso.vmc.project.ProjectListSettings;

import java.io.IOException;
import java.util.Set;

import static java.util.Collections.emptySet;

public class TweevvNotesCurator
        extends CuratorTemplate
{
    @Override
    protected FileSystemVault createVault()
            throws IOException
    {
        return createVaultForPathInUserHome("Notes", "TweeVV");
    }

    @Override
    protected Set<? extends DataModel> createDataModels(Vault vault)
    {
        return emptySet();
    }

    @Override
    protected void registerQueries(QueryCatalog catalog, Vault vault, DataModelMap dataModels)
    {
        catalog.register(new ProjectListQuery(vault, ProjectListSettings.DUTCH));
        catalog.register(new OmniFocusQuery(vault));
        catalog.register(new VolunteersQuery(vault));
    }
}
