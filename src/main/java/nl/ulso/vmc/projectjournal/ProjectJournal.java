package nl.ulso.vmc.projectjournal;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.markdown_curator.*;
import nl.ulso.markdown_curator.journal.*;
import nl.ulso.markdown_curator.project.*;
import nl.ulso.markdown_curator.vault.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;

import static java.util.stream.Collectors.toSet;
import static nl.ulso.markdown_curator.Change.*;
import static nl.ulso.markdown_curator.project.AttributeDefinition.LAST_MODIFIED;
import static nl.ulso.markdown_curator.project.AttributeDefinition.LEAD;
import static nl.ulso.markdown_curator.project.AttributeDefinition.STATUS;
import static nl.ulso.markdown_curator.vault.InternalLinkFinder.parseInternalLinkTargetNames;

/// Keeps track of project attributes - status, lead and last modification date - in the journal.
@Singleton
final class ProjectJournal
    extends ChangeProcessorTemplate
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectJournal.class);

    private static final String PROJECT_STATUSES_MARKER_PROPERTY = "project-statuses";
    private static final String PROJECT_LEADS_MARKER_PROPERTY = "project-leads";
    private static final int WEIGHT = 100;

    private final Journal journal;
    private final ProjectRepository projectRepository;
    private final AttributeDefinition leadDefinition;
    private final AttributeDefinition statusDefinition;
    private final AttributeDefinition lastModifiedDefinition;

    private final Map<String, NavigableMap<LocalDate, String>> projectStatuses;
    private final Set<String> statusMarkers;
    private final Map<String, String> linkToStatusMap;

    private final Map<String, NavigableMap<LocalDate, String>> projectLeads;
    private final Set<String> leadMarkers;
    private final Map<String, String> linkToLeadMap;

    private final Set<String> allMarkers;

    @Inject
    ProjectJournal(
        Journal journal, ProjectRepository projectRepository,
        Map<String, AttributeDefinition> attributeDefinitions)
    {
        this.journal = journal;
        this.projectRepository = projectRepository;
        this.leadDefinition = attributeDefinitions.get(LEAD);
        this.statusDefinition = attributeDefinitions.get(STATUS);
        this.lastModifiedDefinition = attributeDefinitions.get(LAST_MODIFIED);
        this.projectStatuses = new HashMap<>();
        this.statusMarkers = new HashSet<>();
        this.linkToStatusMap = new HashMap<>();
        this.projectLeads = new HashMap<>();
        this.leadMarkers = new HashSet<>();
        this.linkToLeadMap = new HashMap<>();
        this.allMarkers = new HashSet<>();
        this.registerChangeHandler(
            isObjectType(Daily.class).and(isCreateOrUpdate()),
            this::processDailyUpdate
        );
        this.registerChangeHandler(
            isObjectType(Daily.class).and(isDelete()),
            this::processDailyDeletion
        );
        this.registerChangeHandler(
            isObjectType(Project.class).and(isDelete()),
            this::processProjectDeletion
        );
    }

    @Override
    public Set<Class<?>> consumedObjectTypes()
    {
        return Set.of(Daily.class, Marker.class, Project.class);
    }

    @Override
    public Set<Class<?>> producedObjectTypes()
    {
        return Set.of(AttributeValue.class);
    }

    @Override
    protected boolean isFullRefreshRequired(Changelog changelog)
    {
        return changelog.changes().anyMatch(isObjectType(Marker.class));
    }

    @Override
    public Collection<Change<?>> fullRefresh()
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
        var changes = createChangeCollection();
        projectRepository.projects()
            .forEach(project -> collectProjectChanges(changes, project));
        return changes;
    }

    private Collection<Change<?>> processDailyDeletion(Change<?> change)
    {
        var daily = change.objectAs(Daily.class);
        var relatedProjects = projectRepository.projects().stream()
            .filter(project -> daily.refersTo(project.name()))
            .collect(toSet());
        removeAttributesForDate(daily.date(), projectStatuses);
        removeAttributesForDate(daily.date(), projectLeads);
        var changes = createChangeCollection();
        relatedProjects.forEach(project -> collectProjectChanges(changes, project));
        return changes;
    }

    private Collection<Change<?>> processDailyUpdate(Change<?> change)
    {
        var daily = change.objectAs(Daily.class);
        LOGGER.debug("Processing journal entry '{}' for project attributes", daily.date());
        removeAttributesForDate(daily.date(), projectStatuses);
        removeAttributesForDate(daily.date(), projectLeads);
        var changes = createChangeCollection();
        for (Project project : projectRepository.projects())
        {
            var projectName = project.name();
            if (daily.refersTo(projectName))
            {
                var entries = daily.markedLinesFor(projectName, allMarkers, false);
                updateProjectAttributes(projectName, entries);
                collectProjectChanges(changes, project);
            }
        }
        return changes;
    }

    private Collection<Change<?>> processProjectDeletion(Change<?> change)
    {
        var project = (Project) change.object();
        projectStatuses.remove(project.name());
        projectLeads.remove(project.name());
        var changes = createChangeCollection();
        collectProjectChanges(changes, project);
        return changes;
    }

    private void collectProjectChanges(Collection<Change<?>> changes, Project project)
    {
        statusOf(project).ifPresentOrElse(
            status ->
                changes.add(update(
                        new AttributeValue(
                            project,
                            statusDefinition,
                            status,
                            WEIGHT
                        ),
                        AttributeValue.class
                    )
                ), () ->
                changes.add(delete(
                        new AttributeValue(
                            project,
                            statusDefinition,
                            null,
                            WEIGHT
                        ),
                        AttributeValue.class
                    )
                )
        );
        leadOf(project).ifPresentOrElse(lead ->
            changes.add(update(
                    new AttributeValue(
                        project,
                        leadDefinition,
                        lead,
                        WEIGHT
                    ),
                    AttributeValue.class
                )
            ), () ->
            changes.add(delete(
                    new AttributeValue(
                        project,
                        leadDefinition,
                        null,
                        WEIGHT
                    ),
                    AttributeValue.class
                )
            )
        );
        journal.mostRecentMentionOf(project.name()).ifPresentOrElse(date ->
            changes.add(update(
                    new AttributeValue(
                        project,
                        lastModifiedDefinition,
                        date,
                        WEIGHT
                    ),
                    AttributeValue.class
                )
            ), () ->
            changes.add(delete(
                    new AttributeValue(
                        project,
                        lastModifiedDefinition,
                        null,
                        WEIGHT
                    ),
                    AttributeValue.class
                )
            )
        );
    }

    private void discoverMarkers()
    {
        for (var marker : journal.markers().values())
        {
            processFrontMatter(
                marker.document(), PROJECT_STATUSES_MARKER_PROPERTY, statusMarkers, linkToStatusMap
            );
            processFrontMatter(
                marker.document(), PROJECT_LEADS_MARKER_PROPERTY, leadMarkers, linkToLeadMap
            );
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
                        .computeIfAbsent(projectName, _ -> new TreeMap<>())
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

    private interface LineProcessor
    {
        String processLine(String line, String link, String alias);
    }
}
