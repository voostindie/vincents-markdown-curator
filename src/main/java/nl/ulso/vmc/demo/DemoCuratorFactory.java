package nl.ulso.vmc.demo;

import nl.ulso.markdown_curator.Curator;
import nl.ulso.markdown_curator.CuratorFactory;

public class DemoCuratorFactory
        implements CuratorFactory
{
    @Override
    public String name()
    {
        return "DEMO";
    }

    @Override
    public Curator createCurator()
    {
        return DaggerDemoCurator.create().curator();
    }
}
