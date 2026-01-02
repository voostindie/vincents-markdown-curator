package nl.ulso.vmc.tweevv;

import dagger.*;
import dagger.Module;
import dagger.multibindings.IntoSet;
import nl.ulso.markdown_curator.CuratorModule;
import nl.ulso.markdown_curator.ChangeProcessor;
import nl.ulso.markdown_curator.journal.JournalModule;
import nl.ulso.markdown_curator.journal.JournalSettings;
import nl.ulso.markdown_curator.project.ProjectModule;
import nl.ulso.markdown_curator.project.ProjectSettings;
import nl.ulso.markdown_curator.query.Query;
import nl.ulso.vmc.hook.HooksQuery;
import nl.ulso.vmc.omnifocus.OmniFocusModule;
import nl.ulso.vmc.omnifocus.OmniFocusSettings;
import nl.ulso.vmc.projectjournal.ProjectJournalModule;
import nl.ulso.vmc.tweevv.trainers.*;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

import static nl.ulso.markdown_curator.VaultPaths.pathInUserHome;

@Module(includes = {
        CuratorModule.class,
        ProjectModule.class,
        JournalModule.class,
        ProjectJournalModule.class,
        OmniFocusModule.class
})
abstract class TweevvNotesCuratorModule
{
    private static final String PROJECT_FOLDER = "Projecten";
    private static final String MARKER_SUB_FOLDER = "Markeringen";
    private static final String JOURNAL_FOLDER = "Logboek";
    private static final String ACTIVITIES_SECTION = "Activiteiten";

    @Provides
    static Path provideVaultPath()
    {
        return pathInUserHome("TweeVV", "Notes");
    }

    @Provides
    static Locale provideLocale()
    {
        return Locale.forLanguageTag("nl");
    }

    @Binds
    @IntoSet
    abstract ChangeProcessor bindVolunteeringModel(VolunteeringModel volunteeringModel);

    @Binds
    @IntoSet
    abstract ChangeProcessor bindTrainerModel(TrainerModel trainerModel);

    @Binds
    @IntoSet
    abstract Query bindVolunteersQuery(VolunteersQuery volunteersQuery);

    @Binds
    @IntoSet
    abstract Query bindRetiredVolunteersQuery(RetiredVolunteersQuery retiredVolunteersQuery);

    @Binds
    @IntoSet
    abstract Query bindGroupQuery(GroupQuery groupQuery);

    @Binds
    @IntoSet
    abstract Query bindHooksQuery(HooksQuery hooksQuery);

    @Binds
    @IntoSet
    abstract Query bindQualificationQuery(QualificationQuery qualificationQuery);

    @Binds
    @IntoSet
    abstract Query bindTariffGroupQuery(TariffGroupQuery tariffGroupQuery);

    @Binds
    @IntoSet
    abstract Query bindTrainingGroupQuery(TrainingGroupQuery trainingGroupQuery);

    @Binds
    @IntoSet
    abstract Query bindTrainerQuery(TrainerQuery trainerQuery);

    @Binds
    @IntoSet
    abstract Query bindTrainerCsvQuery(TrainerCsvQuery trainerCsvQuery);

    @Provides
    static JournalSettings provideJournalSettings()
    {
        return new JournalSettings(
                JOURNAL_FOLDER,
                MARKER_SUB_FOLDER,
                ACTIVITIES_SECTION,
                PROJECT_FOLDER
        );
    }

    @Provides
    static ProjectSettings provideProjectSettings()
    {
        return new ProjectSettings(PROJECT_FOLDER);
    }

    @Provides
    static OmniFocusSettings provideOmniFocusSettings()
    {
        return new OmniFocusSettings(
                PROJECT_FOLDER,
                "🏐 TweeVV",
                (name) -> !name.startsWith("⚡️") &&
                          !Set.of("🤖 Routine",
                                  "🏐 Diversen",
                                  "💶 Declaraties",
                                  "✉️ Eerstvolgende nieuwsbrief").contains(name));
    }
}
