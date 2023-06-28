package nl.ulso.vmc.personal;

import nl.ulso.markdown_curator.CuratorModule;
import nl.ulso.markdown_curator.journal.JournalModule;
import nl.ulso.vmc.hook.HooksQuery;
import nl.ulso.vmc.jxa.JxaClasspathRunner;
import nl.ulso.vmc.jxa.JxaRunner;
import nl.ulso.vmc.obsidian.StarredDocumentsQuery;

import java.nio.file.Path;

public class PersonalNotesCuratorModule
        extends CuratorModule
{
    private static final String JOURNAL_FOLDER = "Journal";
    private static final String ACTIVITIES_SECTION = "Activities";

    @Override
    public String name()
    {
        return "Personal";
    }

    @Override
    public Path vaultPath()
    {
        return iCloudIAWriterFolder("Personal");
    }

    @Override
    protected void configureCurator()
    {
        bind(JxaRunner.class).to(JxaClasspathRunner.class);
        install(new JournalModule(JOURNAL_FOLDER, ACTIVITIES_SECTION));
        registerDataModel(Library.class);
        registerQuery(ReadingQuery.class);
        registerQuery(BooksQuery.class);
        registerQuery(HooksQuery.class);
        registerQuery(StarredDocumentsQuery.class);
    }
}
