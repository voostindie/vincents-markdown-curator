package nl.ulso.vmc.projectjournal;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import nl.ulso.markdown_curator.DataModel;
import nl.ulso.markdown_curator.journal.JournalModule;
import nl.ulso.markdown_curator.project.AttributeValueResolver;
import nl.ulso.markdown_curator.project.ProjectModule;

/**
 * Combines the Project and Journal modules by offering additional functionality on top of them to
 * extract project attributes from the journal instead of from front matter.
 */
@Module(includes = {
        JournalModule.class,
        ProjectModule.class
})
public abstract class ProjectJournalModule
{
    @Binds
    @IntoSet
    abstract DataModel bindProjectJournal(ProjectJournal projectJournal);

    @Binds
    @IntoSet
    abstract AttributeValueResolver<?> bindLastModifiedAttributeValueResolver(
            JournalLastModifiedAttributeValueResolver resolver);

    @Binds
    @IntoSet
    abstract AttributeValueResolver<?> bindStatusAttributeValueResolver(
            JournalStatusAttributeValueResolver resolver);

    @Binds
    @IntoSet
    abstract AttributeValueResolver<?> bindLeadAttributeValueResolver(
            JournalLeadAttributeValueResolver resolver);
}
