package nl.ulso.vmc.personal;

import nl.ulso.markdown_curator.CuratorModule;

import java.nio.file.Path;

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
