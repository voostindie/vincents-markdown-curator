package nl.ulso.vmc.rabobank;

import nl.ulso.curator.Curator;
import nl.ulso.curator.CuratorFactory;

public class RabobankNotesCuratorFactory
        implements CuratorFactory
{
    @Override
    public String name()
    {
        return "Rabobank";
    }

    @Override
    public Curator createCurator()
    {
        return DaggerRabobankNotesCurator.create().curator();
    }
}
