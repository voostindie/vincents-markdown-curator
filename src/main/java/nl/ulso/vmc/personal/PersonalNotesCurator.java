package nl.ulso.vmc.personal;

import dagger.Component;
import nl.ulso.markdown_curator.Curator;

import javax.inject.Singleton;

@Singleton
@Component(modules = PersonalNotesCuratorModule.class)
interface PersonalNotesCurator
{
    Curator curator();
}
