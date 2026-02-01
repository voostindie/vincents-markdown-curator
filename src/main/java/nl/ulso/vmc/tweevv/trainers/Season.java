package nl.ulso.vmc.tweevv.trainers;

import nl.ulso.curator.vault.Document;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

/**
 * Represents a single season and holds all trainer and related data for that season.
 * <p/>
 * Almost all logic applies to a single season. By having all data on trainers and reference data
 * already grouped per season, this logic is straightforward to implement.
 * <p/>
 * Changes to a season are meant to be applied by the {@link TrainerModel} only. For all others
 * seasons are meant to be read-only.
 */
public final class Season
{
    private final Document document;
    private final Map<String, TariffGroup> tariffGroups;
    private final Map<String, Qualification> qualifications;
    private final Map<String, TrainingGroup> trainingGroups;
    private final Map<String, Trainer> trainers;

    Season(Document document)
    {
        this.document = document;
        this.tariffGroups = new HashMap<>();
        this.trainingGroups = new HashMap<>();
        this.qualifications = new HashMap<>();
        this.trainers = new HashMap<>();
    }

    public String name()
    {
        return document.name();
    }

    void addTariffGroup(Document tariffGroupDocument, BigDecimal amount)
    {
        var tariffGroup = new TariffGroup(tariffGroupDocument, amount);
        tariffGroups.put(tariffGroup.name(), tariffGroup);
    }

    void addQualification(Document qualificationDocument, BigDecimal amount)
    {
        var qualificationAllowance = new Qualification(qualificationDocument, amount);
        qualifications.put(qualificationAllowance.name(), qualificationAllowance);
    }

    void addTrainingGroup(
        Document trainingGroupDocument, String tariffGroupName, BigDecimal practicesPerWeek)
    {
        var tariffGroup = tariffGroups.get(tariffGroupName);
        if (tariffGroup == null)
        {
            return;
        }
        var trainingGroup = new TrainingGroup(trainingGroupDocument, tariffGroup, practicesPerWeek);
        trainingGroups.put(trainingGroup.name(), trainingGroup);
    }

    void addAssignment(Document trainerDocument, String trainingGroupName, BigDecimal factor)
    {
        var trainingGroup = trainingGroups.get(trainingGroupName);
        if (trainingGroup == null)
        {
            return;
        }
        var trainer = resolveTrainer(trainerDocument);
        trainer.addAssignment(trainingGroup, factor);
    }

    void addQualification(Document trainerDocument, String qualificationName)
    {
        var qualification = qualifications.get(qualificationName);
        if (qualification == null)
        {
            return;
        }
        var trainer = resolveTrainer(trainerDocument);
        trainer.addQualification(qualification);
    }

    public Stream<Qualification> qualifications()
    {
        return qualifications.values().stream();
    }

    public Stream<TariffGroup> tariffGroups()
    {
        return tariffGroups.values().stream();
    }

    public Stream<TrainingGroup> trainingGroups()
    {
        return trainingGroups.values().stream();
    }

    public Stream<Trainer> trainers()
    {
        return trainers.values().stream();
    }

    private Trainer resolveTrainer(Document trainerDocument)
    {
        return trainers.computeIfAbsent(trainerDocument.name(),
            key -> new Trainer(trainerDocument)
        );
    }

    public Optional<Trainer> trainerFor(Document document)
    {
        return Optional.ofNullable(trainers.get(document.name()));
    }

    public Stream<Trainer> trainersWith(Qualification qualification)
    {
        return trainers.values().stream()
            .filter(trainer -> trainer.hasQualification(qualification));
    }

    public Stream<Trainer> trainersFor(TrainingGroup trainingGroup)
    {
        return trainers.values().stream()
            .filter(trainer -> trainer.isAssignedTo(trainingGroup));
    }

    public Stream<TrainingGroup> trainingGroupsFor(TariffGroup tariffGroup)
    {
        return trainingGroups.values().stream()
            .filter(trainingGroup -> trainingGroup.tariffGroup().equals(tariffGroup));
    }
}
