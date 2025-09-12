package nl.ulso.vmc.projectjournal;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import nl.ulso.markdown_curator.DataModel;
import nl.ulso.markdown_curator.journal.JournalModule;
import nl.ulso.markdown_curator.project.ProjectPropertyResolver;
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
    abstract ProjectPropertyResolver bindLastModifiedAttributeValueResolver(
            JournalLastModifiedProjectPropertyResolver resolver);

    @Binds
    @IntoSet
    abstract ProjectPropertyResolver bindStatusAttributeValueResolver(
            JournalStatusProjectPropertyResolver resolver);

    @Binds
    @IntoSet
    abstract ProjectPropertyResolver bindLeadAttributeValueResolver(
            JournalLeadProjectPropertyResolver resolver);
}
