package nl.ulso.vmc.tweevv.trainers;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.markdown_curator.DataModelTemplate;
import nl.ulso.markdown_curator.FrontMatterUpdateCollector;
import nl.ulso.markdown_curator.vault.*;
import nl.ulso.markdown_curator.vault.event.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static nl.ulso.markdown_curator.vault.InternalLinkFinder.parseInternalLinkTargetNames;

@Singleton
public final class TrainerModel
        extends DataModelTemplate

{
    public static final String UNDER_16_PROPERTY = "under16";
    // For now all names of folders and sections are hardcoded.
    private static final String MODEL_FOLDER = "Trainersvergoedingen";
    private static final String SEASON_FOLDER = "Seizoenen";
    private static final String TARIFF_GROUP_FOLDER = "Tariefgroepen";
    private static final String TRAINING_GROUP_FOLDER = "Trainingsgroepen";
    private static final String QUALIFICATION_FOLDER = "Kwalificatietoeslagen";
    private static final String TRAINER_FOLDER = "Contacten";
    private static final String TARIFF_SECTION = "Tarieven";
    private static final String PRACTICE_SECTION = "Trainingen";
    private static final String ACTIVITY_SECTION = "Taken";
    private static final String QUALIFICATION_SECTION = "Kwalificaties";
    private static final String TRAINER_DOCUMENT = "Trainer";

    // "2025-2026"
    private static final Predicate<String> SEASON_PREDICATE =
            Pattern.compile("^\\d{4}-\\d{4}$").asMatchPredicate();
    // "€250,-", "€125,50"
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("^€(\\d+),(\\d{2}+|-)$");
    // "[[Tariff group]] 1 keer per week", "[[Tariff group]] 2 keer per week"
    private static final Pattern SINGLE_PRACTICE_PATTERN = Pattern.compile(
            "^\\[\\[.*]] (\\d+) keer per week$");
    // "[[Tariff group]] 3 keer per 2 weken"
    private static final Pattern MULTI_PRACTICE_PATTERN = Pattern.compile(
            "^\\[\\[.*]] (\\d+) keer per (\\d+) weken$");
    // "[[Trainer]] [[Training group]]", "[[Trainer]] [[Training group]] (50%)"
    private static final Pattern TRAINER_PATTERN = Pattern.compile(
            "^(\\[\\[.*]]) (\\[\\[.*]])( \\((\\d+)%\\))?$");
    public static final String IBAN_PROPERTY = "iban";
    public static final String EMAIL_PROPERTY = "email";
    public static final String COC_PROPERTY = "vog";

    private final Vault vault;
    private final FrontMatterUpdateCollector frontMatterUpdateCollector;
    private final Map<String, Season> seasons;

    @Inject
    public TrainerModel(Vault vault, FrontMatterUpdateCollector frontMatterUpdateCollector)
    {
        this.vault = vault;
        this.frontMatterUpdateCollector = frontMatterUpdateCollector;
        this.seasons = new HashMap<>();
    }

    @Override
    public void fullRefresh()
    {
        seasons.clear();
        vault.folder(MODEL_FOLDER).ifPresent(modelFolder -> {
            modelFolder.folder(SEASON_FOLDER).ifPresent(this::importSeasons);
            modelFolder.folder(TARIFF_GROUP_FOLDER).ifPresent(this::importTariffGroups);
            modelFolder.folder(QUALIFICATION_FOLDER).ifPresent(this::importQualifications);
            modelFolder.folder(TRAINING_GROUP_FOLDER).ifPresent(this::importTrainingGroups);
        });
        vault.folder(TRAINER_FOLDER).ifPresent(this::importTrainers);
        vault.folder(TRAINER_FOLDER).ifPresent(this::updateTrainerFrontMatter);
    }

    @Override
    public void process(FolderAdded event)
    {
        // Do nothing
    }

    @Override
    public void process(FolderRemoved event)
    {
        processEventInFolder(event.folder());
    }

    @Override
    public void process(DocumentAdded event)
    {
        processEventInFolder(event.document().folder());
    }

    @Override
    public void process(DocumentChanged event)
    {
        processEventInFolder(event.document().folder());
    }

    @Override
    public void process(DocumentRemoved event)
    {
        processEventInFolder(event.document().folder());
    }

    /*
     * Do a full refresh if the event is in the main #{MODEL_FOLDER} or in the #{TRAINER_FOLDER},
     * otherwise ignore the event.
     */

    void processEventInFolder(Folder folder)
    {
        if (isFolderInPath(folder, MODEL_FOLDER) || isFolderInPath(folder, TRAINER_FOLDER))
        {
            fullRefresh();
        }
    }

    private boolean isFolderInPath(Folder folder, String folderName)
    {
        var currentFolder = folder;
        while (currentFolder != vault)
        {
            if (currentFolder.name().equals(folderName))
            {
                return true;
            }
            currentFolder = currentFolder.parent();
        }
        return false;
    }

    private void importSeasons(Folder folder)
    {
        folder.documents().stream()
                .filter(document -> SEASON_PREDICATE.test(document.name()))
                .map(Season::new)
                .forEach(s -> seasons.put(s.name(), s));
    }

    private void importTariffGroups(Folder folder)
    {
        folder.documents().forEach(document ->
                document.accept(new SectionVisitor(TARIFF_SECTION, (season, text) ->
                        parseAmount(text).ifPresent(amount ->
                                season.addTariffGroup(document, amount)
                        )))
        );
    }

    private void importQualifications(Folder folder)
    {
        folder.documents().forEach(document ->
                document.accept(new SectionVisitor(TARIFF_SECTION, (season, text) ->
                        parseAmount(text).ifPresent(amount ->
                                season.addQualification(document, amount)
                        )))
        );
    }

    private void importTrainingGroups(Folder folder)
    {
        folder.documents().forEach(document ->
                document.accept(new SectionVisitor(PRACTICE_SECTION, (season, text) -> {
                    var tariffGroup = parseInternalLinkTargetNames(text).iterator().next();
                    int count;
                    int weeks;
                    var matcher = SINGLE_PRACTICE_PATTERN.matcher(text);
                    if (matcher.find())
                    {
                        count = parseInt(matcher.group(1));
                        weeks = 1;
                    }
                    else
                    {
                        matcher = MULTI_PRACTICE_PATTERN.matcher(text);
                        if (!matcher.find())
                        {
                            return;
                        }
                        count = parseInt(matcher.group(1));
                        weeks = parseInt(matcher.group(2));
                    }
                    var practicesPerWeek = BigDecimal.valueOf(((double) count) / weeks);
                    season.addTrainingGroup(document, tariffGroup, practicesPerWeek);
                }))
        );
    }

    private void importTrainers(Folder folder)
    {
        folder.documents().forEach(document ->
                {
                    document.accept(new SectionVisitor(ACTIVITY_SECTION, (season, text) -> {
                        var matcher = TRAINER_PATTERN.matcher(text);
                        if (!matcher.find())
                        {
                            return;
                        }
                        var trainer =
                                parseInternalLinkTargetNames(matcher.group(1)).iterator().next();
                        if (!trainer.contentEquals(TRAINER_DOCUMENT))
                        {
                            return;
                        }
                        var trainingGroupName =
                                parseInternalLinkTargetNames(matcher.group(2)).iterator().next();
                        var percentageString = matcher.group(4);
                        var percentage = percentageString != null ? parseInt(percentageString) :
                                         100;
                        season.addAssignment(document, trainingGroupName,
                                BigDecimal.valueOf(((double) percentage) / 100));
                    }));
                    document.accept(new SectionVisitor(QUALIFICATION_SECTION, (season, text) -> {
                        var links = parseInternalLinkTargetNames(text);
                        if (links.isEmpty())
                        {
                            return;
                        }
                        var qualification = links.iterator().next();
                        season.addQualification(document, qualification);
                    }));
                }
        );
    }

    private void updateTrainerFrontMatter(Folder folder)
    {
        for (Document document : folder.documents())
        {
            frontMatterUpdateCollector.updateFrontMatterFor(document, dictionary -> {
                dictionary.removeProperty(IBAN_PROPERTY);
                dictionary.removeProperty(EMAIL_PROPERTY);
                dictionary.removeProperty(COC_PROPERTY);
                dictionary.removeProperty(UNDER_16_PROPERTY);
                var matched = new HashSet<String>();
                for (Season season : seasons.values())
                {
                    season.trainerFor(document).ifPresent(trainer ->
                    {
                        if (matched.contains(trainer.name()))
                        {
                            return;
                        }
                        trainer.iban().ifPresent(iban ->
                                dictionary.setProperty(IBAN_PROPERTY, iban));
                        trainer.email().ifPresent(email ->
                                dictionary.setProperty(EMAIL_PROPERTY, email));
                        trainer.certificateOfConductDate().ifPresent(date ->
                                dictionary.setProperty(COC_PROPERTY, date.toString()));
                        if (trainer.isUnder16())
                        {
                            dictionary.setProperty(UNDER_16_PROPERTY, true);
                        }
                        matched.add(trainer.name());
                    });
                }
            });
        }
    }

    private Optional<BigDecimal> parseAmount(String text)
    {
        var matcher = AMOUNT_PATTERN.matcher(text);
        if (!matcher.find())
        {
            return Optional.empty();
        }
        var euros = parseInteger(matcher.group(1));
        if (euros == -1)
        {
            return Optional.empty();
        }
        var centString = matcher.group(2);
        var cents = centString.contentEquals("-") ? 0 : parseInteger(centString);
        if (cents == -1)
        {
            return Optional.empty();
        }
        return Optional.of(new BigDecimal(euros + ((double) cents) / 100));
    }

    private static int parseInteger(String euroString)
    {
        try
        {
            return parseInt(euroString);
        }
        catch (NumberFormatException e)
        {
            return -1;
        }
    }

    @Override
    public int order()
    {
        return ORDER_LAST;
    }

    public Season season(String seasonName)
    {
        return seasons.get(seasonName);
    }

    private interface SeasonLineProcessor
    {
        void processSeasonLine(Season season, String text);
    }

    private final class SectionVisitor
            extends BreadthFirstVaultVisitor
    {
        private final String sectionName;
        private final SeasonLineProcessor processor;

        public SectionVisitor(String sectionName, SeasonLineProcessor processor)
        {
            this.sectionName = sectionName;
            this.processor = processor;
        }

        @Override
        public void visit(Section section)
        {
            if (section.level() == 1)
            {
                super.visit(section);
            }
            if (section.level() == 2 && section.sortableTitle().contentEquals(sectionName))
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
                    return;
                }
                var seasonLink = parseInternalLinkTargetNames(line.substring(2, colon));
                if (seasonLink.isEmpty())
                {
                    return;
                }
                var season = TrainerModel.this.seasons.get(seasonLink.iterator().next());
                if (season == null)
                {
                    return;
                }
                processor.processSeasonLine(season, line.substring(colon + 1).trim());
            });
        }
    }
}