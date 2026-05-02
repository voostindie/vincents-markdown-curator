package nl.ulso.vmc.omnifocus;

import dagger.*;
import dagger.Module;
import dagger.multibindings.*;
import jakarta.inject.Singleton;
import nl.ulso.curator.addon.project.ProjectAttributeDefinition;
import nl.ulso.curator.addon.project.ProjectModule;
import nl.ulso.curator.change.ChangeProcessor;
import nl.ulso.curator.query.Query;
import nl.ulso.jxa.JavaScriptForAutomationModule;

import static nl.ulso.curator.addon.project.ProjectAttributeDefinition.newAttributeDefinition;
import static nl.ulso.vmc.omnifocus.OmniFocusAttributeProducer.OMNIFOCUS_URL_ATTRIBUTE;

@Module(includes = {
    JavaScriptForAutomationModule.class,
    ProjectModule.class
})
public abstract class OmniFocusModule
{
    @Binds
    abstract OmniFocusMessages bindOmniFocusMessages(ResourceBundleOmniFocusMessages messages);

    @Provides
    @Singleton
    @IntoMap
    @StringKey(OMNIFOCUS_URL_ATTRIBUTE)
    static ProjectAttributeDefinition provideOmniFocusUrl()
    {
        return newAttributeDefinition(String.class, "omnifocus");
    }

    @Binds
    @IntoSet
    abstract ChangeProcessor bindOmniFocusInitializer(OmniFocusInitializer producer);

    @Binds
    @IntoSet
    abstract ChangeProcessor bindOmniFocusAttributeProducer(OmniFocusAttributeProducer producer);

    @Binds
    @IntoSet
    abstract Query bindOmniFocusQuery(OmniFocusQuery omniFocusQuery);

}
