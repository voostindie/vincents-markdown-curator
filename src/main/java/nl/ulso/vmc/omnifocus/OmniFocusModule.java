package nl.ulso.vmc.omnifocus;

import dagger.*;
import dagger.Module;
import dagger.multibindings.*;
import jakarta.inject.Singleton;
import nl.ulso.markdown_curator.project.*;
import nl.ulso.markdown_curator.query.Query;
import nl.ulso.vmc.jxa.JxaModule;

import java.util.Map;

import static nl.ulso.markdown_curator.project.ProjectProperty.newProperty;
import static nl.ulso.vmc.omnifocus.OmniFocusUrlResolver.OMNIFOCUS_URL;

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

    @Provides
    @Singleton
    @IntoMap
    @StringKey(OMNIFOCUS_URL)
    static ProjectProperty provideOmniFocusUrlProperty()
    {
        return newProperty(String.class, "omnifocus");
    }

    @Provides
    @IntoSet
    static ProjectPropertyResolver provideOmniFocusUrlResolver(
            Map<String, ProjectProperty> properties, OmniFocusRepository repository)
    {
        return new OmniFocusUrlResolver(properties.get(OMNIFOCUS_URL), repository);
    }

    @Binds
    @IntoSet
    abstract Query bindOmniFocusQuery(OmniFocusQuery omniFocusQuery);

}
