package nl.ulso.vmc.tweevv;

import dagger.Component;
import jakarta.inject.Singleton;
import nl.ulso.markdown_curator.Curator;

@Singleton
@Component(modules = TweevvNotesCuratorModule.class)
interface TweevvNotesCurator
{
    Curator curator();
}
