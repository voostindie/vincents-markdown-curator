package nl.ulso.vmc.tweevv.trainers;

import nl.ulso.curator.vault.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static nl.ulso.curator.vault.InternalLinkFinder.parseInternalLinkTargetNames;

/**
 * Represents a trainer in a specific season.
 * <p/>
 * A better name would maybe be "TrainerInSeason", but, ah well...
 * <p/>
 * There's some waste in processing here: if the same trainer is present in multiple seasons, then
 * the processing of the personal data takes place multiple times, and the same data is stored
 * several times. I don't see this as a big problem, however, given the limited amount of data to
 * process, all in memory. Maybe in the next decade or so... It's also not hard to solve, but the
 * code would be more complex. So, for later. Maybe.
 * <p/>
 * A trainer is considered to be read-only, except for the {@link TrainerModel} that manages it.
 */
public final class Trainer
{
    private final Document document;
    private final Set<Qualification> qualifications;
    private final Set<Assignment> assignments;
    private final String email;
    private final String iban;
    private final LocalDate certificateOfConductDate;
    private final String residency;
    private final boolean under16;
    private final boolean coach;

    public Trainer(Document document)
    {
        this.document = document;
        var finder = new PersonalDataFinder();
        document.accept(finder);
        this.email = finder.email;
        this.iban = finder.iban;
        this.certificateOfConductDate = finder.certificateOfConductDate;
        this.residency = finder.residency;
        this.under16 = finder.under16;
        this.coach = finder.coach;
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

    public Optional<String> iban()
    {
        return Optional.ofNullable(iban);
    }

    public Optional<String> email()
    {
        return Optional.ofNullable(email);
    }

    public Optional<LocalDate> certificateOfConductDate()
    {
        return Optional.ofNullable(certificateOfConductDate);
    }

    public Optional<String> residency()
    {
        return Optional.ofNullable(residency);
    }

    public boolean isUnder16()
    {
        return under16;
    }

    public boolean isCoach()
    {
        return coach;
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

    /**
     * Computes the trainer's compensation.
     *
     * @return The total compensation for a trainer in a season.
     */
    public BigDecimal computeCompensation()
    {
        var total = BigDecimal.ZERO;
        for (Assignment assignment : assignments)
        {
            total = total.add(assignment.computeCompensation());
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

    /**
     * Extracts personal data for a trainer from a document; the data is expected to be in an
     * unordered list in a section with the name {@link #PERSONAL_DATA_SECTION}.
     * <p/>
     * In the wiki, every personal data field is actually a reference to a document representing
     * that field. This ensures that typos can't be made, and that for each field there's room for a
     * bit of explanation as to why we're administering it.
     */
    private static class PersonalDataFinder
        extends BreadthFirstVaultVisitor
    {
        private static final String PERSONAL_DATA_SECTION = "Persoonsgegevens";
        private static final String IBAN_DOCUMENT = "IBAN";
        private static final String EMAIL_DOCUMENT = "E-mail";
        private static final String COC_DOCUMENT = "VOG";
        private static final String RESIDENCY_DOCUMENT = "Woonplaats";
        private static final String UNDER_16_DOCUMENT = "Onder 16";
        private static final String COACH_DOCUMENT = "Coach";

        private String email;
        private String iban;
        private LocalDate certificateOfConductDate;
        private String residency;
        private boolean under16 = false;
        private boolean coach = false;

        @Override
        public void visit(Section section)
        {
            if (section.level() == 1)
            {
                super.visit(section);
            }
            if (section.level() == 2 &&
                section.sortableTitle().contentEquals(PERSONAL_DATA_SECTION))
            {
                super.visit(section);
            }
        }

        @Override
        public void visit(TextBlock textBlock)
        {
            textBlock.markdown().trim().lines().forEach(line ->
            {
                if (!line.startsWith("- "))
                {
                    return;
                }
                var colon = line.indexOf(": ");
                if (colon == -1)
                {
                    // This is just a marker; there's no data attached to the field
                    var links = parseInternalLinkTargetNames(line.substring(2));
                    if (links.size() != 1)
                    {
                        return;
                    }
                    else if (links.contains(UNDER_16_DOCUMENT))
                    {
                        under16 = true;
                    }
                    else if (links.contains(COACH_DOCUMENT))
                    {
                        coach = true;
                    }
                    return;
                }
                // This is a field with data attached to it, after the colon
                var links = parseInternalLinkTargetNames(line.substring(2, colon));
                if (links.size() != 1)
                {
                    return;
                }
                var attribute = links.iterator().next();
                var value = line.substring(colon + 1).trim();
                if (attribute.contentEquals(IBAN_DOCUMENT))
                {
                    this.iban = value;
                }
                else if (attribute.contentEquals(EMAIL_DOCUMENT))
                {
                    this.email = value;
                }
                else if (attribute.contentEquals(COC_DOCUMENT))
                {
                    this.certificateOfConductDate = LocalDates.parseDateOrNull(value);
                }
                else if (attribute.contentEquals(RESIDENCY_DOCUMENT))
                {
                    this.residency = value;
                }
            });
        }
    }
}
