package nl.ulso.vmc.tweevv;

import nl.ulso.markdown_curator.*;
import nl.ulso.markdown_curator.query.QueryCatalog;
import nl.ulso.markdown_curator.vault.FileSystemVault;
import nl.ulso.markdown_curator.vault.Vault;
import nl.ulso.vmc.omnifocus.OmniFocusQuery;
import nl.ulso.vmc.omnifocus.OmniFocusSettings;
import nl.ulso.vmc.project.ProjectListQuery;
import nl.ulso.vmc.project.ProjectListSettings;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class TweevvNotesCurator
        extends CuratorTemplate
{
    private static final String PROJECT_FOLDER = "Projecten";

    @Override
    protected FileSystemVault createVault()
            throws IOException
    {
        return createVaultForPathInUserHome("Notes", "TweeVV");
    }

    @Override
    protected Set<DataModel> createDataModels(Vault vault)
    {
        return Set.of(new VolunteeringModel(vault));
    }

    @Override
    protected void registerQueries(QueryCatalog catalog, Vault vault, DataModelMap dataModels)
    {
        catalog.register(new ProjectListQuery(vault, new ProjectListSettings(
                PROJECT_FOLDER,
                "Activiteiten",
                "Datum",
                "Project")));
        catalog.register(new OmniFocusQuery(vault, new OmniFocusSettings(
                PROJECT_FOLDER,
                "🏐 TweeVV",
                List.of(
                        "🤖 Routine",
                        "🏐 Diversen",
                        "💶 Declaraties"
                ))));
        catalog.register(new VolunteersQuery(dataModels.get(VolunteeringModel.class)));
        catalog.register(new GroupQuery(dataModels.get(VolunteeringModel.class)));
    }
}
