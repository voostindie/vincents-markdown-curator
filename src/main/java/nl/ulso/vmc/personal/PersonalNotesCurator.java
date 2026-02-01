package nl.ulso.vmc.personal;

import dagger.Component;
import jakarta.inject.Singleton;
import nl.ulso.curator.Curator;

@Singleton
@Component(modules = PersonalNotesCuratorModule.class)
interface PersonalNotesCurator
{
    Curator curator();
}
