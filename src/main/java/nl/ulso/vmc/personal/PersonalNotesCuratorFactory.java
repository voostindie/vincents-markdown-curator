package nl.ulso.vmc.personal;

import nl.ulso.curator.Curator;
import nl.ulso.curator.CuratorFactory;

public class PersonalNotesCuratorFactory
        implements CuratorFactory
{
    @Override
    public String name()
    {
        return "Personal";
    }

    @Override
    public Curator createCurator()
    {
        return DaggerPersonalNotesCurator.create().curator();
    }
}
