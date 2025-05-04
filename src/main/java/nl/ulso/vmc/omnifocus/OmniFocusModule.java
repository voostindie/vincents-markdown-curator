package nl.ulso.vmc.omnifocus;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import nl.ulso.markdown_curator.project.AttributeValueResolver;
import nl.ulso.markdown_curator.project.ProjectModule;
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
    abstract AttributeValueResolver<?> bindPriorityAttributeValueResolver(
            OmniFocusPriorityAttributeValueResolver resolver);

    @Binds
    @IntoSet
    abstract AttributeValueResolver<?> bindStatusAttributeValueResolver(
            OmniFocusStatusAttributeValueResolver resolver);

    @Binds
    @IntoSet
    abstract Query bindOmniFocusQuery(OmniFocusQuery omniFocusQuery);

}
