package nl.ulso.vmc.omnifocus;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import nl.ulso.markdown_curator.project.ProjectModule;
import nl.ulso.markdown_curator.project.ProjectPropertyResolver;
import nl.ulso.markdown_curator.query.Query;
import nl.ulso.vmc.jxa.JxaModule;

@Module(includes = {
        JxaModule.class,
        ProjectModule.class
})
public abstract class OmniFocusModule
{
    @Binds
    abstract OmniFocusMessages bindOmniFocusMessages(ResourceBundleOmniFocusMessages messages);

    @Binds
    @IntoSet
    abstract ProjectPropertyResolver bindPriorityProjectPropertyValueResolver(
            OmniFocusPriorityProjectPropertyResolver resolver);

    @Binds
    @IntoSet
    abstract ProjectPropertyResolver bindStatusProjectPropertyValueResolver(
            OmniFocusStatusProjectPropertyResolver resolver);

    @Binds
    @IntoSet
    abstract Query bindOmniFocusQuery(OmniFocusQuery omniFocusQuery);

}
