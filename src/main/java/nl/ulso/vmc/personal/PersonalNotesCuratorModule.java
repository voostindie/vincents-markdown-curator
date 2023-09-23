package nl.ulso.vmc.personal;

import nl.ulso.markdown_curator.CuratorModule;
import nl.ulso.markdown_curator.journal.JournalModule;
import nl.ulso.markdown_curator.links.LinksModule;
import nl.ulso.vmc.hook.HooksQuery;
import nl.ulso.vmc.jxa.JxaClasspathRunner;
import nl.ulso.vmc.jxa.JxaRunner;

import java.nio.file.Path;

public class PersonalNotesCuratorModule
        extends CuratorModule
{
    private static final String JOURNAL_FOLDER = "Journal";
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
        return pathInUserHome("Notes", "Personal");
    }

    @Override
    protected void configureCurator()
    {
        install(new JournalModule(JOURNAL_FOLDER, ACTIVITIES_SECTION, PROJECT_FOLDER));
        install(new LinksModule());
        bind(JxaRunner.class).to(JxaClasspathRunner.class);
        registerDataModel(Library.class);
        registerQuery(ReadingQuery.class);
        registerQuery(BooksQuery.class);
        registerQuery(HooksQuery.class);
    }
}
