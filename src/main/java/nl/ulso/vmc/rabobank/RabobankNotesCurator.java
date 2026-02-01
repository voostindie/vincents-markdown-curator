package nl.ulso.vmc.rabobank;

import dagger.Component;
import jakarta.inject.Singleton;
import nl.ulso.curator.Curator;


@Singleton
@Component(modules = RabobankNotesCuratorModule.class)
interface RabobankNotesCurator
{
    Curator curator();
}
