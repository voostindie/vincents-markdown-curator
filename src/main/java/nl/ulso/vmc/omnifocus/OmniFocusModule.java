package nl.ulso.vmc.omnifocus;

import dagger.*;
import dagger.Module;
import dagger.multibindings.*;
import jakarta.inject.Singleton;
import nl.ulso.markdown_curator.project.*;
import nl.ulso.markdown_curator.query.Query;
import nl.ulso.vmc.jxa.JavaScriptForAutomationModule;

import static nl.ulso.markdown_curator.project.ProjectProperty.newProperty;
import static nl.ulso.vmc.omnifocus.OmniFocusUrlResolver.OMNIFOCUS_URL;

@Module(includes = {
        JavaScriptForAutomationModule.class,
        ProjectModule.class
})
public abstract class OmniFocusModule
{
    @Binds
    abstract OmniFocusMessages bindOmniFocusMessages(ResourceBundleOmniFocusMessages messages);

    @Binds
    @IntoSet
    abstract ProjectPropertyResolver bindOmniFocusPriorityResolver(
            OmniFocusPriorityResolver resolver);

    @Binds
    @IntoSet
    abstract ProjectPropertyResolver bindOmniFocusStatusResolver(
            OmniFocusStatusResolver resolver);

    @Binds
    @IntoSet
    abstract ProjectPropertyResolver bindOmniFocusUrlResolver(
            OmniFocusUrlResolver resolver);


    @Provides
    @Singleton
    @IntoMap
    @StringKey(OMNIFOCUS_URL)
    static ProjectProperty provideOmniFocusUrlProperty()
    {
        return newProperty(String.class, "omnifocus");
    }

    @Binds
    @IntoSet
    abstract Query bindOmniFocusQuery(OmniFocusQuery omniFocusQuery);

}
