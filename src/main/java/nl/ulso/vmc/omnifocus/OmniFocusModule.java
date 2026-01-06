package nl.ulso.vmc.omnifocus;

import dagger.*;
import dagger.Module;
import dagger.multibindings.*;
import jakarta.inject.Singleton;
import nl.ulso.jxa.JavaScriptForAutomationModule;
import nl.ulso.markdown_curator.ChangeProcessor;
import nl.ulso.markdown_curator.ExternalChangeObjectType;
import nl.ulso.markdown_curator.project.AttributeDefinition;
import nl.ulso.markdown_curator.project.ProjectModule;
import nl.ulso.markdown_curator.query.Query;

import static nl.ulso.markdown_curator.project.AttributeDefinition.newAttributeDefinition;
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
    static AttributeDefinition provideOmniFocusUrl()
    {
        return newAttributeDefinition(String.class, "omnifocus");
    }

    @Provides
    @IntoSet
    @ExternalChangeObjectType
    static Class<?> provideOmniFocusChangeObjectType()
    {
        return OmniFocusUpdate.class;
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
