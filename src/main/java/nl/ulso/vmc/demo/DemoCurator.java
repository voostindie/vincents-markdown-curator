package nl.ulso.vmc.demo;

import dagger.Component;
import nl.ulso.curator.Curator;

import jakarta.inject.Singleton;

@Singleton
@Component(modules = DemoCuratorModule.class)
public interface DemoCurator
{
    Curator curator();
}
