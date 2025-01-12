package nl.ulso.vmc.graph;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import nl.ulso.markdown_curator.DataModel;
import nl.ulso.markdown_curator.query.Query;

/**
 * Extracts a graph from the journal.
 * To use this journal in your own curator, you have to do install it in your Guice module.
 */
@Module
public abstract class MermaidGraphModule
{
    @Binds
    @IntoSet
    abstract DataModel bindMermaidGraph(MermaidGraph mermaidGraph);

    @Binds
    @IntoSet
    abstract Query bindMermaidGraphQuery(MermaidGraphQuery mermaidGraphQuery);
}
