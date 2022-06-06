package nl.ulso.vmc.rabobank;

import nl.ulso.markdown_curator.*;
import nl.ulso.markdown_curator.query.QueryCatalog;
import nl.ulso.markdown_curator.vault.FileSystemVault;
import nl.ulso.markdown_curator.vault.Vault;
import nl.ulso.vmc.omnifocus.OmniFocusQuery;
import nl.ulso.vmc.project.ProjectListQuery;
import nl.ulso.vmc.project.ProjectListSettings;

import java.io.IOException;
import java.util.Set;

public class RabobankNotesCurator
        extends CuratorTemplate
{
    @Override
    protected FileSystemVault createVault()
            throws IOException
    {
        return createVaultForPathInUserHome("Notes", "Rabobank");
    }

    @Override
    protected Set<DataModel> createDataModels(Vault vault)
    {
        return Set.of(
                new Journal(vault),
                new OrgChart(vault)
        );
    }

    @Override
    protected void registerQueries(QueryCatalog catalog, Vault vault, DataModelMap dataModels)
    {
        catalog.register(new OmniFocusQuery(vault));
        catalog.register(new ProjectListQuery(vault, ProjectListSettings.ENGLISH));
        catalog.register(new ArticlesQuery(vault));
        catalog.register(new SystemsQuery(vault));
        catalog.register(new ArchitectureDecisionRecordsQuery(vault));
        catalog.register(new OneOnOneQuery(vault));
        catalog.register(new WeeklyQuery(dataModels.get(Journal.class)));
        catalog.register(new SubteamsQuery(dataModels.get(OrgChart.class)));
        catalog.register(new RolesQuery(dataModels.get(OrgChart.class)));
    }

    public static void main(String[] args)
    {
        new RabobankNotesCurator().runOnce();
    }
}
