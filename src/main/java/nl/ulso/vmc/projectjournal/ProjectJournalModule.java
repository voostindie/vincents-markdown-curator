package nl.ulso.vmc.projectjournal;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoSet;
import nl.ulso.markdown_curator.ChangeProcessor;
import nl.ulso.markdown_curator.journal.JournalModule;
import nl.ulso.markdown_curator.project.ProjectModule;

/// Combines the Project and Journal modules by offering additional functionality to extract project
/// attributes from the journal instead of from front matter.
///
/// This module overrides the default resolvers for the project lead, status and last modification
/// date.
@Module(includes = {
    JournalModule.class,
    ProjectModule.class
})
public abstract class ProjectJournalModule
{
    @Binds
    @IntoSet
    abstract ChangeProcessor bindProjectJournal(ProjectJournal projectJournal);
}
