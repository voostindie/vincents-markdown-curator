package nl.ulso.vmc.rabobank;

import dagger.Component;
import nl.ulso.markdown_curator.Curator;

import javax.inject.Singleton;

@Singleton
@Component(modules = RabobankNotesCuratorModule.class)
interface RabobankNotesCurator
{
    Curator curator();
}
