package nl.ulso.vmc.tweevv;

import nl.ulso.markdown_curator.DataModelTemplate;
import nl.ulso.markdown_curator.vault.*;
import nl.ulso.markdown_curator.vault.event.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
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
    private final Map<Season, Map<Contact, Set<Activity>>> volunteering;

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
        super.process(event);
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
            volunteering.values().forEach(map -> map.remove(contact));
        }
        else if (parentFolderName.equals(TEAMS_FOLDER))
        {
            var activity = activities.remove(document.name());
            volunteering.values().forEach(map -> map.values().forEach(set -> set.remove(activity)));
        }
    }

    public Map<Contact, Set<Activity>> volunteersFor(String seasonString)
    {
        return Season.fromString(seasonString)
                .map(season -> unmodifiableMap(volunteering.getOrDefault(season, emptyMap())))
                .orElse(emptyMap());
    }

    public Stream<Contact> contactsFor(Season season, String activityName)
    {
        var activity = activities.get(activityName);
        return volunteering.computeIfAbsent(season, s -> emptyMap())
                .entrySet().stream()
                .filter(entry -> entry.getValue().contains(activity))
                .map(Map.Entry::getKey)
                .sorted(comparing(Contact::name));
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
        private final String name;
        private final Document document;

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
            if (section.level() == 2 && section.title().contentEquals("Taken"))
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
            var lines = textBlock.lines();
            for (var line : lines)
            {
                if (!line.startsWith("- "))
                {
                    continue;
                }
                var colon = line.indexOf(": ");
                if (colon == -1)
                {
                    continue;
                }
                var seasonString = line.substring(2, colon).trim();
                var activityText = line.substring(colon + 1).trim();
                Season.fromString(seasonString).ifPresent(season ->
                        volunteering.computeIfAbsent(season, s -> new HashMap<>())
                                .computeIfAbsent(contact, c -> new HashSet<>())
                                .add(resolveActivity(activityText)));
            }
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
