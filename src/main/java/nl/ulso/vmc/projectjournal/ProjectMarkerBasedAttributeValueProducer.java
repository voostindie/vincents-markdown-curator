package nl.ulso.vmc.projectjournal;

import nl.ulso.curator.addon.journal.*;
import nl.ulso.curator.addon.project.*;
import nl.ulso.curator.change.ChangeHandler;

import java.util.*;

import static nl.ulso.curator.change.Change.isPayloadType;
import static nl.ulso.curator.change.ChangeHandler.newChangeHandler;

abstract class ProjectMarkerBasedAttributeValueProducer<P extends ProjectMarker>
    extends ProjectJournalAttributeValueProducer
{
    private final ProjectMarkerRepository<P> projectMarkerRepository;

    ProjectMarkerBasedAttributeValueProducer(
        ProjectRepository projectRepository,
        Journal journal,
        ProjectAttributeDefinition attributeDefinition,
        ProjectMarkerRepository<P> projectMarkerRepository)
    {
        super(projectRepository, journal, attributeDefinition);
        this.projectMarkerRepository = projectMarkerRepository;
    }

    @Override
    public final Set<Class<?>> consumedPayloadTypes()
    {
        var payloadTypes = new HashSet<>(super.consumedPayloadTypes());
        payloadTypes.add(projectMarkerType());
        return payloadTypes;
    }

    @Override
    protected final List<? extends ChangeHandler> createChangeHandlers()
    {
        var handlers = new ArrayList<ChangeHandler>();
        handlers.add(newChangeHandler(
            isPayloadType(projectMarkerType()),
            (_, collector) -> reload(collector)
        ));
        handlers.addAll(super.createChangeHandlers());
        return handlers;
    }

    @Override
    final Optional<Object> resolveAttributeValue(Project project, Daily daily)
    {
        var entries = daily.markedLinesFor(
            project.name(),
            projectMarkerRepository.allMarkers(),
            false
        );
        for (Map.Entry<String, List<MarkedLine>> entry : entries.entrySet())
        {
            var marker = projectMarkerRepository.markerNamed(entry.getKey());
            var markedLines = entry.getValue().reversed();
            for (var markedLine : markedLines)
            {
                var value = findMarkerLink(marker, markedLine.line())
                    .flatMap(link -> resolveAttributeValue(marker, link, markedLine.line()));
                if (value.isPresent())
                {
                    return value;
                }
            }
        }
        return Optional.empty();
    }

    private Optional<String> findMarkerLink(P marker, String line)
    {
        for (var link : marker.markdownLinks().keySet())
        {
            if (line.contains(link))
            {
                return Optional.of(link);
            }
        }
        return Optional.empty();
    }

    protected abstract Optional<Object> resolveAttributeValue(P marker, String link, String line);

    protected abstract Class<P> projectMarkerType();
}
