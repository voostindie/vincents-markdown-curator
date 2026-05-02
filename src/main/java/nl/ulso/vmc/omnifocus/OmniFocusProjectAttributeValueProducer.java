package nl.ulso.vmc.omnifocus;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.addon.project.*;
import nl.ulso.curator.change.*;

import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static nl.ulso.curator.addon.project.ProjectAttributeDefinition.PRIORITY;
import static nl.ulso.curator.addon.project.ProjectAttributeDefinition.STATUS;
import static nl.ulso.curator.change.Change.isDelete;
import static nl.ulso.curator.change.Change.isPayloadType;
import static nl.ulso.curator.change.ChangeHandler.newChangeHandler;
import static nl.ulso.vmc.omnifocus.Status.ON_HOLD;

/// Produces attribute values for projects from matching projects in OmniFocus.
///
/// 3 attributes are produced:
///
/// - URL to the OmniFocus project
/// - Priority of the project in OmniFocus
/// - Status of the project in OmniFocus, but only if the project is "on hold".
@Singleton
final class OmniFocusProjectAttributeValueProducer
    extends ChangeProcessorTemplate
{
    static final String OMNIFOCUS_URL_ATTRIBUTE = "omnifocus";
    private static final int WEIGHT = 200;

    private final ProjectAttributeDefinition urlAttribute;
    private final ProjectAttributeDefinition statusAttribute;
    private final ProjectAttributeDefinition priorityAttribute;
    private final ProjectRepository projectRepository;
    private final OmniFocusRepository omniFocusRepository;
    private final OmniFocusMessages messages;

    @Inject
    OmniFocusProjectAttributeValueProducer(
        Map<String, ProjectAttributeDefinition> attributeDefinitions,
        ProjectRepository projectRepository,
        OmniFocusRepository omniFocusRepository,
        OmniFocusMessages messages)
    {
        this.urlAttribute = requireNonNull(attributeDefinitions.get(OMNIFOCUS_URL_ATTRIBUTE));
        this.statusAttribute = requireNonNull(attributeDefinitions.get(STATUS));
        this.priorityAttribute = requireNonNull(attributeDefinitions.get(PRIORITY));
        this.projectRepository = projectRepository;
        this.omniFocusRepository = omniFocusRepository;
        this.messages = messages;
    }

    @Override
    protected Set<? extends ChangeHandler> createChangeHandlers()
    {
        return Set.of(
            newChangeHandler(
                isPayloadType(OmniFocusUpdate.class),
                this::processOmniFocusProjects
            ),
            newChangeHandler(
                isPayloadType(Project.class).and(isDelete()),
                this::processProjectDelete
            )
        );
    }

    @Override
    public Set<Class<?>> consumedPayloadTypes()
    {
        return Set.of(OmniFocusUpdate.class, Project.class);
    }

    @Override
    public Set<Class<?>> producedPayloadTypes()
    {
        return Set.of(ProjectAttributeValue.class);
    }

    @Override
    protected boolean isResetRequired(Changelog changelog)
    {
        return false;
    }

    private void processOmniFocusProjects(Change<?> change, ChangeCollector collector)
    {
        omniFocusRepository.projects().forEach(omniFocusProject ->
        {
            var project = projectRepository.projectsByName().get(omniFocusProject.name());
            if (project != null)
            {
                createUrl(project, omniFocusProject, collector);
                createPriority(project, omniFocusProject, collector);
                if (omniFocusProject.status() == ON_HOLD)
                {
                    createStatus(project, collector);
                }
                else
                {
                    deleteStatus(project, collector);
                }
            }
        });
    }

    private void processProjectDelete(Change<?> change, ChangeCollector collector)
    {
        var project = change.as(Project.class).value();
        deleteUrl(project, collector);
        deletePriority(project, collector);
        deleteStatus(project, collector);
    }

    private void createUrl(
        Project project, OmniFocusProject omniFocusProject, ChangeCollector collector)
    {
        collector.create(
            new ProjectAttributeValue(
                project,
                urlAttribute,
                omniFocusProject.link(),
                WEIGHT
            ),
            ProjectAttributeValue.class
        );
    }

    private void deleteUrl(Project project, ChangeCollector collector)
    {
        collector.delete(
            new ProjectAttributeValue(
                project,
                urlAttribute,
                null,
                WEIGHT
            ),
            ProjectAttributeValue.class
        );
    }

    private void createPriority(
        Project project, OmniFocusProject omniFocusProject, ChangeCollector collector)
    {
        collector.create(
            new ProjectAttributeValue(
                project,
                priorityAttribute,
                omniFocusProject.priority(),
                WEIGHT
            ),
            ProjectAttributeValue.class
        );
    }

    private void deletePriority(Project project, ChangeCollector collector)
    {
        collector.delete(
            new ProjectAttributeValue(
                project,
                priorityAttribute,
                null,
                WEIGHT
            ),
            ProjectAttributeValue.class
        );
    }

    private void createStatus(Project project, ChangeCollector collector)
    {
        collector.create(
            new ProjectAttributeValue(
                project,
                statusAttribute,
                messages.projectOnHold(),
                WEIGHT
            ),
            ProjectAttributeValue.class
        );
    }

    private void deleteStatus(Project project, ChangeCollector collector)
    {
        collector.delete(
            new ProjectAttributeValue(
                project,
                statusAttribute,
                null,
                WEIGHT
            ),
            ProjectAttributeValue.class
        );
    }
}
