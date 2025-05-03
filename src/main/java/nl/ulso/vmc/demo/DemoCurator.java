package nl.ulso.vmc.demo;

import dagger.Component;
import nl.ulso.markdown_curator.Curator;

import jakarta.inject.Singleton;

@Singleton
@Component(modules = DemoCuratorModule.class)
public interface DemoCurator
{
    Curator curator();
}
