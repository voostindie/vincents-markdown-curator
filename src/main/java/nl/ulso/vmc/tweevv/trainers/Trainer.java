package nl.ulso.vmc.tweevv.trainers;

import nl.ulso.markdown_curator.vault.Document;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

public final class Trainer
{
    private final Document document;
    private final Set<Qualification> qualifications;
    private final Set<Assignment> assignments;

    public Trainer(Document document)
    {
        this.document = document;
        this.qualifications = new HashSet<>();
        this.assignments = new HashSet<>();
    }

    String name()
    {
        return document.name();
    }

    public String link()
    {
        return document.link();
    }

    public Document document()
    {
        return document;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass())
        {
            return false;
        }
        var that = (Trainer) obj;
        return Objects.equals(this.document, that.document);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(document);
    }

    void addAssignment(TrainingGroup group, BigDecimal factor)
    {
        assignments.add(new Assignment(group, factor));
    }

    void addQualification(Qualification qualification)
    {
        qualifications.add(qualification);
    }

    public BigDecimal computeCompensation()
    {
        var total = BigDecimal.ZERO;
        for (Assignment assignment : assignments)
        {
            var singleTariff = assignment.trainingGroup().tariffGroup().tariff();
            var totalTariff = singleTariff.multiply(assignment.trainingGroup().practicesPerWeek());
            var compensation = totalTariff.multiply(assignment.factor());
            total = total.add(compensation);
        }
        for (Qualification qualification : qualifications)
        {
            total = total.add(qualification.allowance());
        }
        return total;
    }

    public Stream<Assignment> assignments()
    {
        return assignments.stream();
    }

    public Stream<Qualification> qualifications()
    {
        return qualifications.stream();
    }

    public boolean hasQualification(Qualification qualification)
    {
        return qualifications.contains(qualification);
    }

    public boolean isAssignedTo(TrainingGroup trainingGroup)
    {
        return assignments.stream()
                .anyMatch(assignment -> assignment.trainingGroup().equals(trainingGroup));
    }

    public BigDecimal factorFor(TrainingGroup trainingGroup)
    {
        return assignments.stream()
                .filter(assignment -> assignment.trainingGroup().equals(trainingGroup))
                .findFirst()
                .map(Assignment::factor)
                .orElse(BigDecimal.ZERO);
    }
}
