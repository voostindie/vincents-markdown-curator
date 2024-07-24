package nl.ulso.vmc.tweevv;

import dagger.Component;
import nl.ulso.markdown_curator.Curator;

import javax.inject.Singleton;

@Singleton
@Component(modules = TweevvNotesCuratorModule.class)
interface TweevvNotesCurator
{
    Curator curator();
}
