package nl.ulso.vmc.projectjournal;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.markdown_curator.DataModelTemplate;
import nl.ulso.markdown_curator.journal.Journal;
import nl.ulso.markdown_curator.journal.MarkedLine;
import nl.ulso.markdown_curator.project.Project;
import nl.ulso.markdown_curator.project.ProjectRepository;
import nl.ulso.markdown_curator.vault.Document;
import nl.ulso.markdown_curator.vault.LocalDates;
import nl.ulso.markdown_curator.vault.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;

import static nl.ulso.markdown_curator.vault.InternalLinkFinder.parseInternalLinkTargetNames;

/// Keeps track of project attributes - status and lead - in the journal.
///
/// The achievement of the day (May 4, 2025) is that this functionality now works. I've had it on my
/// wishlist for a long time, but before I could start the implementation, I had to refactor the
/// complete code base to accommodate for it. I did that yesterday. Today this functionality works.
/// It will need further refactoring, optimization and test cases before I can bring it to the core
/// curator. That's an adventure for another day.
@Singleton
final class ProjectJournal
    extends DataModelTemplate
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectJournal.class);

    private static final String PROJECT_STATUSES_MARKER_PROPERTY = "project-statuses";
    private static final String PROJECT_LEADS_MARKER_PROPERTY = "project-leads";

    private final Journal journal;
    private final ProjectRepository projectRepository;

    private final Map<String, NavigableMap<LocalDate, String>> projectStatuses;
    private final Set<String> statusMarkers;
    private final Map<String, String> linkToStatusMap;

    private final Map<String, NavigableMap<LocalDate, String>> projectLeads;
    private final Set<String> leadMarkers;
    private final Map<String, String> linkToLeadMap;

    private final Set<String> allMarkers;

    @Inject
    ProjectJournal(Journal journal, ProjectRepository projectRepository)
    {
        this.journal = journal;
        this.projectRepository = projectRepository;
        this.projectStatuses = new HashMap<>();
        this.statusMarkers = new HashSet<>();
        this.linkToStatusMap = new HashMap<>();
        this.projectLeads = new HashMap<>();
        this.leadMarkers = new HashSet<>();
        this.linkToLeadMap = new HashMap<>();
        this.allMarkers = new HashSet<>();
    }

    @Override
    public void fullRefresh()
    {
        this.projectStatuses.clear();
        this.statusMarkers.clear();
        this.linkToStatusMap.clear();
        this.projectLeads.clear();
        this.leadMarkers.clear();
        this.linkToLeadMap.clear();
        this.allMarkers.clear();
        discoverMarkers();
        this.allMarkers.addAll(statusMarkers);
        this.allMarkers.addAll(leadMarkers);
        if (!allMarkers.isEmpty())
        {
            processJournal();
        }
    }

    @Override
    public void process(FolderAdded event)
    {
        // There's nothing to do if a folder is added.
    }

    @Override
    public void process(FolderRemoved event)
    {
        // If a folder is removed, we only care about that if it concerns the project repository.
        if (projectRepository.projects().isEmpty())
        {
            super.process(event);
        }
    }

    @Override
    public void process(DocumentAdded event)
    {
        processDocumentChangeEvent(event.document());
    }

    @Override
    public void process(DocumentChanged event)
    {
        processDocumentChangeEvent(event.document());
    }

    private void processDocumentChangeEvent(Document document)
    {
        if (journal.isMarkerDocument(document))
        {
            // Every time a marker document is changed, we need to re-discover the markers and
            // process the complete journal again.
            fullRefresh();
        }
        else if (journal.isJournalEntry(document))
        {
            // Every time a journal entry is changed, we need to process it.
            journal.toDaily(document).ifPresent(daily ->
            {
                LOGGER.debug("Processing journal entry '{}' for project attributes",
                    document.name()
                );
                removeAttributesForDate(daily.date(), projectStatuses);
                removeAttributesForDate(daily.date(), projectLeads);
                for (Project project : projectRepository.projects())
                {
                    var projectName = project.name();
                    if (daily.refersTo(projectName))
                    {
                        var entries = daily.markedLinesFor(projectName, allMarkers, false);
                        updateProjectAttributes(projectName, entries);
                    }
                }
            });
        }
    }

    @Override
    public void process(DocumentRemoved event)
    {
        var document = event.document();
        if (journal.isMarkerDocument(document))
        {
            // When a marker document is removed, we need to re-discover all markers and process
            // the complete journal again.
            fullRefresh();
        }
        else if (journal.isJournalEntry(document))
        {
            // When a journal entry is removed, we need to remove all associated attributes.
            var date = LocalDates.parseDateOrNull(document.name());
            if (date == null)
            {
                return;
            }
            removeAttributesForDate(date, projectStatuses);
            removeAttributesForDate(date, projectLeads);
        }
        else if (projectRepository.isProjectDocument(document))
        {
            // If a project is removed, we should also remove it from the caches.
            var projectName = document.name();
            projectStatuses.remove(projectName);
            projectLeads.remove(projectName);
        }
    }

    private void discoverMarkers()
    {
        for (var document : journal.markers().values())
        {
            processFrontMatter(document, PROJECT_STATUSES_MARKER_PROPERTY, statusMarkers,
                linkToStatusMap
            );
            processFrontMatter(document, PROJECT_LEADS_MARKER_PROPERTY, leadMarkers, linkToLeadMap);
        }
    }

    private void processFrontMatter(
        Document document,
        String propertyName,
        Set<String> markers,
        Map<String, String> linkToAliases)
    {
        var frontMatter = document.frontMatter();
        if (frontMatter.hasProperty(propertyName))
        {
            markers.add(document.name());
            var aliases = frontMatter.listOfStrings(propertyName);
            for (String alias : aliases)
            {
                var link = "[[" + document.name() + "|" + alias + "]]";
                linkToAliases.put(link, alias);
            }
        }
    }

    private void processJournal()
    {
        // This is an expensive operation: for every active project we go through the whole journal
        // and extract all project attributes. Luckily, we only do that on startup and when a full
        // refresh is needed.
        for (Project project : projectRepository.projects())
        {
            var projectName = project.name();
            var entries = journal.markedLinesFor(projectName, allMarkers, false);
            updateProjectAttributes(projectName, entries);
        }
    }

    private void updateProjectAttributes(
        String projectName, Map<String, List<MarkedLine>> entries)
    {
        LOGGER.debug("Extracting attributes of project '{}' from the journal", projectName);
        for (Map.Entry<String, List<MarkedLine>> entry : entries.entrySet())
        {
            var markedLines = entry.getValue();
            for (MarkedLine markedLine : markedLines)
            {
                processMarkedLineFor(projectName, markedLine, linkToStatusMap, projectStatuses,
                    this::extractProjectStatus
                );
                processMarkedLineFor(projectName, markedLine, linkToLeadMap, projectLeads,
                    this::extractProjectLead
                );
            }
        }
    }

    private void processMarkedLineFor(
        String projectName,
        MarkedLine markedLine,
        Map<String, String> linkToAliasMap,
        Map<String, NavigableMap<LocalDate, String>> attributes,
        LineProcessor lineProcessor)
    {
        for (Map.Entry<String, String> linkToAlias : linkToAliasMap.entrySet())
        {
            var link = linkToAlias.getKey();
            var alias = linkToAlias.getValue();
            if (markedLine.line().contains(link))
            {
                var value = lineProcessor.processLine(markedLine.line(), link, alias);
                if (value != null)
                {
                    // Note that every date has at most one entry, which means that if there are
                    // multiple marked lines, only one is kept. That's the last one, and that's the
                    // most recent one. The fair assumption here is that the journal is written in
                    // chronological order.
                    attributes
                        .computeIfAbsent(projectName, k -> new TreeMap<>())
                        .put(markedLine.date(), value);
                }
            }
        }
    }

    private String extractProjectStatus(String line, String link, String alias)
    {
        // The status of a project is the alias itself.
        return alias;
    }

    private String extractProjectLead(String line, String link, String alias)
    {
        // The lead of a project is the (hopefully) one and only internal link in the line
        var links = parseInternalLinkTargetNames(line.replace(link, ""));
        if (links.isEmpty())
        {
            LOGGER.warn("Found no internal link in line '{}'. This line is ignored!", line);
            return null;
        }
        if (links.size() > 1)
        {
            LOGGER.warn(
                "Found more than one internal link in line '{}'. Results can be unpredictable!",
                line
            );
        }
        return links.iterator().next();
    }

    private void removeAttributesForDate(
        LocalDate date, Map<String, NavigableMap<LocalDate, String>> attributes)
    {
        attributes.values().forEach(entries -> entries.remove(date));
    }

    Optional<String> statusOf(Project project)
    {
        return valueOf(project, projectStatuses);
    }

    Optional<Document> leadOf(Project project)
    {
        return valueOf(project, projectLeads)
            .flatMap(lead -> journal.vault().findDocument(lead));
    }

    private <T> Optional<T> valueOf(
        Project project, Map<String, NavigableMap<LocalDate, T>> entries)
    {
        var entry = entries.get(project.name());
        if (entry == null)
        {
            return Optional.empty();
        }
        var latest = entry.lastEntry();
        if (latest == null)
        {
            return Optional.empty();
        }
        return Optional.of(latest.getValue());
    }

    @Override
    public int order()
    {
        return Math.max(journal.order(), projectRepository.order()) + 1;
    }

    private interface LineProcessor
    {
        String processLine(String line, String link, String alias);
    }
}
