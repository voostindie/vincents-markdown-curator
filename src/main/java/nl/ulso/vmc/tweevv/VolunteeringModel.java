package nl.ulso.vmc.tweevv;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.markdown_curator.*;
import nl.ulso.markdown_curator.vault.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.Character.isDigit;
import static java.lang.Integer.parseInt;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toSet;
import static nl.ulso.markdown_curator.Change.Kind.CREATION;
import static nl.ulso.markdown_curator.Change.Kind.MODIFICATION;

@Singleton
public class VolunteeringModel
    extends DataModelTemplate
{
    private static final String TEAM_FOLDER = "Teams";
    private static final String CONTACT_FOLDER = "Contacten";

    private final Vault vault;
    private final Map<String, Activity> activities;
    private final Map<String, Contact> contacts;
    private final Map<Season, Set<ContactActivity>> volunteering;

    @Inject
    VolunteeringModel(Vault vault)
    {
        this.vault = vault;
        this.activities = new HashMap<>();
        this.contacts = new HashMap<>();
        this.volunteering = new HashMap<>();
        registerChangeHandler(isTeamDocument(), this::processTeamUpdate);
        registerChangeHandler(isContactDocument(), this::processContactUpdate);
    }

    private Predicate<Change<?>> isTeamDocument()
    {
        return hasObjectType(Document.class).and(change ->
        {
            var document = (Document) change.object();
            return document.folder().name().equals(TEAM_FOLDER);
        });
    }

    private Predicate<Change<?>> isContactDocument()
    {
        return hasObjectType(Document.class).and(change ->
        {
            var document = (Document) change.object();
            return document.folder().name().equals(CONTACT_FOLDER);
        });
    }

    @Override
    public Collection<Change<?>> fullRefresh()
    {
        activities.clear();
        contacts.clear();
        volunteering.clear();
        vault.folder(TEAM_FOLDER).ifPresent(folder ->
        {
            for (Document document : folder.documents())
            {
                addActivity(document);
            }
        });
        vault.folder(CONTACT_FOLDER).ifPresent(folder ->
        {
            for (Document document : folder.documents())
            {
                addContact(document);
            }
        });
        return emptyList();
    }

    private Collection<Change<?>> processTeamUpdate(Change<?> change)
    {
        var document = (Document) change.object();
        var activity = activities.remove(document.name());
        volunteering.values().forEach(set ->
            set.removeIf(contactActivity -> contactActivity.activity.equals(activity)));
        if (change.kind() == CREATION || change.kind() == MODIFICATION)
        {
            addActivity(document);
            volunteering.clear();
            for (Contact contact : contacts.values())
            {
                new ActivityProcessor(contact).process();
            }
        }
        return emptyList();
    }

    private Collection<Change<?>> processContactUpdate(Change<?> change)
    {
        var document = (Document) change.object();
        var contact = contacts.remove(document.name());
        volunteering.values().forEach(set ->
            set.removeIf(contactActivity -> contactActivity.contact.equals(contact)));
        if (change.kind() == CREATION || change.kind() == MODIFICATION)
        {
            addContact(document);
        }
        return emptyList();
    }

    private void addActivity(Document document)
    {
        var activity = new Activity(document);
        activities.put(activity.name(), activity);
    }

    private void addContact(Document document)
    {
        var contact = new Contact(document);
        contacts.put(contact.name(), contact);
        new ActivityProcessor(contact).process();
    }

    public Map<Contact, List<ContactActivity>> volunteersFor(String seasonString)
    {
        return volunteersFor(seasonString, null);
    }

    public Map<Contact, List<ContactActivity>> volunteersFor(
        String seasonString, String activityName)
    {
        Activity selectedActivity = activityName != null ? activities.get(activityName) : null;
        if (activityName != null && selectedActivity == null)
        {
            return emptyMap();
        }
        return Season.fromString(seasonString)
            .map(season -> unmodifiableSet(volunteering.getOrDefault(season, emptySet())))
            .orElse(emptySet())
            .stream()
            .filter(ca -> selectedActivity == null || ca.activity == selectedActivity)
            .sorted(comparing(contactActivity -> contactActivity.contact.name()))
            .collect(Collectors.groupingBy(ContactActivity::contact));
    }

    public Map<Contact, List<ContactActivity>> retiredVolunteersFor(String seasonString)
    {
        return Season.fromString(seasonString)
            .map(activeSeason ->
            {
                var activeVolunteers = volunteering.getOrDefault(activeSeason, emptySet()).stream()
                    .map(ContactActivity::contact).collect(toSet());
                var priorSeason = Season.forStartYear(activeSeason.startYear - 1);
                return volunteering.getOrDefault(priorSeason, emptySet()).stream()
                    .filter(ca -> !activeVolunteers.contains(ca.contact()))
                    .sorted(comparing(activity -> activity.contact.name()))
                    .collect(Collectors.groupingBy(ContactActivity::contact));
            })
            .orElse(emptyMap());
    }

    public static class Season
    {
        private static final Map<Integer, Season> CACHE = new HashMap<>();
        private final int startYear;

        private Season(int startYear)
        {
            this.startYear = startYear;
        }

        public static Season forStartYear(int startYear)
        {
            return CACHE.computeIfAbsent(startYear, _ -> new Season(startYear));
        }

        /*
         * Seasons should be strings of the format "<year>-<year + 1>", but we're lenient about it.
         * We just look for the first 4 digits in the string and interpret them as the first year,
         * and then go from there.
         * <p/>
         * As of December 2025, seasons have their own documents, to support the trainer
         * compensations. This method is compatible with that. The two systems co-exist, for
         * backwards compatibility reasons. How the two will be merged into one, if ever, is not yet
         * clear.
         */
        public static Optional<Season> fromString(String seasonString)
        {
            Objects.requireNonNull(seasonString);
            var length = seasonString.length();
            // Find the first digit
            var start = 0;
            while (start < length)
            {
                if (isDigit(seasonString.charAt(start)))
                {
                    break;
                }
                start++;
            }
            // Ensure there are at least 4 characters from the first digit
            if (start + 4 > length)
            {
                return Optional.empty();
            }
            // Parse 4 specific characters as an integer and make a Season out of it.
            try
            {
                int startYear = parseInt(seasonString.substring(start, start + 4));
                return Optional.of(forStartYear(startYear));
            }
            catch (NumberFormatException e)
            {
                return Optional.empty();
            }
        }

        @Override
        public String toString()
        {
            return startYear + "-" + (startYear + 1);
        }
    }

    public static class Activity
    {
        private final Document document;
        private final String name;

        private Activity(Document document, String name)
        {
            this.document = document;
            this.name = name;
        }

        public Activity(Document document)
        {
            this(document, document.name());
        }

        public Activity(String name)
        {
            this(null, name);
        }

        public String name()
        {
            return name;
        }

        public Optional<Document> document()
        {
            return Optional.ofNullable(document);
        }

        public String toMarkdown()
        {
            if (document != null)
            {
                return document.link();
            }
            return name;
        }
    }

    public record Contact(Document document)
    {
        public String name()
        {
            return document.name();
        }

        public String link()
        {
            return document.link();
        }
    }

    public record ContactActivity(Contact contact, Activity activity, String description)
    {

        public String shortDescription()
        {
            return description.replace(activity.toMarkdown(), "").trim();
        }
    }

    public class ActivityProcessor
        extends BreadthFirstVaultVisitor
    {
        private final Contact contact;
        private boolean inSection;

        public ActivityProcessor(Contact contact)
        {
            this.contact = contact;
            this.inSection = false;
        }

        public void process()
        {
            contact.document.accept(this);
        }

        @Override
        public void visit(Section section)
        {
            if (section.level() == 2 && section.sortableTitle().contentEquals("Taken"))
            {
                inSection = true;
                super.visit(section);
                inSection = false;
            }
        }

        @Override
        public void visit(TextBlock textBlock)
        {
            if (!inSection)
            {
                return;
            }
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
                var seasonString = line.substring(2, colon).trim();
                var activityText = line.substring(colon + 1).trim();
                Season.fromString(seasonString).ifPresent(season ->
                    volunteering.computeIfAbsent(season, _ -> new HashSet<>()).add(
                        new ContactActivity(
                            contact,
                            resolveActivity(activityText),
                            activityText
                        )));

            });
        }

        private Activity resolveActivity(String activityText)
        {
            var activityName = activityText;
            var start = activityText.indexOf("[[");
            if (start != -1)
            {
                int end = activityText.indexOf("]]", start + 2);
                if (end != -1)
                {
                    activityName = activityText.substring(start + 2, end);
                }
            }
            return activities.computeIfAbsent(activityName, Activity::new);
        }
    }
}
