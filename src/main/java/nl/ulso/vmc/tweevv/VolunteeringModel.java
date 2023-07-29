package nl.ulso.vmc.tweevv;

import nl.ulso.markdown_curator.DataModelTemplate;
import nl.ulso.markdown_curator.vault.*;
import nl.ulso.markdown_curator.vault.event.*;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static java.util.Comparator.comparing;
import static nl.ulso.markdown_curator.vault.event.VaultChangedEvent.documentAdded;
import static nl.ulso.markdown_curator.vault.event.VaultChangedEvent.folderAdded;

@Singleton
public class VolunteeringModel
        extends DataModelTemplate
{
    private static final String TEAMS_FOLDER = "Teams";
    private static final String CONTACTS_FOLDER = "Contacten";
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
    }

    @Override
    protected void fullRefresh()
    {
        activities.clear();
        contacts.clear();
        volunteering.clear();
        vault.folder(TEAMS_FOLDER).ifPresent(folder ->
        {
            for (Document document : folder.documents())
            {
                addActivity(document);
            }
        });
        vault.folder(CONTACTS_FOLDER).ifPresent(folder ->
        {
            for (Document document : folder.documents())
            {
                addContact(document);
            }
        });
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

    @Override
    public void process(FolderAdded event)
    {
        var folderName = event.folder().name();
        if (folderName.equals(TEAMS_FOLDER) || folderName.equals(CONTACTS_FOLDER))
        {
            fullRefresh();
        }
    }

    @Override
    public void process(FolderRemoved event)
    {
        process(folderAdded(event.folder()));
    }

    @Override
    public void process(DocumentAdded event)
    {
        var document = event.document();
        var parentFolderName = document.folder().name();
        if (parentFolderName.equals(CONTACTS_FOLDER))
        {
            addContact(document);
        }
        else if (parentFolderName.equals(TEAMS_FOLDER))
        {
            addActivity(document);
            volunteering.clear();
            for (Contact contact : contacts.values())
            {
                new ActivityProcessor(contact).process();
            }
        }
    }

    @Override
    public void process(DocumentChanged event)
    {
        process(documentAdded(event.document()));
    }

    @Override
    public void process(DocumentRemoved event)
    {
        var document = event.document();
        var parentFolderName = document.folder().name();
        if (parentFolderName.equals(CONTACTS_FOLDER))
        {
            var contact = contacts.remove(document.name());
            volunteering.values().forEach(set ->
                    set.removeIf(contactActivity -> contactActivity.contact.equals(contact)));
        }
        else if (parentFolderName.equals(TEAMS_FOLDER))
        {
            var activity = activities.remove(document.name());
            volunteering.values().forEach(set ->
                    set.removeIf(contactActivity -> contactActivity.activity.equals(activity)));
        }
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
            return CACHE.computeIfAbsent(startYear, year -> new Season(startYear));
        }

        public static Optional<Season> fromString(String seasonString)
        {
            Objects.requireNonNull(seasonString);
            if (seasonString.length() != 9)
            {
                return Optional.empty();
            }
            try
            {
                int startYear = Integer.parseInt(seasonString.substring(0, 4));
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
                        volunteering.computeIfAbsent(season, s -> new HashSet<>()).add(
                                new ContactActivity(
                                        contact,
                                        resolveActivity(activityText),
                                        activityText)));

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
