package nl.ulso.vmc.omnifocus;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.markdown_curator.*;
import nl.ulso.markdown_curator.project.*;

import java.util.*;

import static nl.ulso.markdown_curator.Change.create;
import static nl.ulso.markdown_curator.Change.delete;
import static nl.ulso.markdown_curator.Change.isDelete;
import static nl.ulso.markdown_curator.Change.isObjectType;
import static nl.ulso.markdown_curator.project.AttributeDefinition.PRIORITY;
import static nl.ulso.markdown_curator.project.AttributeDefinition.STATUS;
import static nl.ulso.vmc.omnifocus.Status.ON_HOLD;

/// Produces attribute values for projects from matching projects in OmniFocus.
///
/// 3 attributes are produced:
///
/// - URL to the OmniFocus project
/// - Priority of the project in OmniFocus
/// - Status of the project in OmniFocus, but only if the project is "on hold".
@Singleton
final class OmniFocusAttributeProducer
    extends ChangeProcessorTemplate
{
    static final String OMNIFOCUS_URL_ATTRIBUTE = "omnifocus";
    private static final int WEIGHT = 200;

    private final AttributeDefinition urlAttribute;
    private final AttributeDefinition statusAttribute;
    private final AttributeDefinition priorityAttribute;
    private final ProjectRepository projectRepository;
    private final OmniFocusRepository omniFocusRepository;
    private final OmniFocusMessages messages;

    @Inject
    OmniFocusAttributeProducer(
        Map<String, AttributeDefinition> attributeDefinitions,
        ProjectRepository projectRepository,
        OmniFocusRepository omniFocusRepository,
        OmniFocusMessages messages)
    {
        this.urlAttribute = attributeDefinitions.get(OMNIFOCUS_URL_ATTRIBUTE);
        this.statusAttribute = attributeDefinitions.get(STATUS);
        this.priorityAttribute = attributeDefinitions.get(PRIORITY);
        this.projectRepository = projectRepository;
        this.omniFocusRepository = omniFocusRepository;
        this.messages = messages;
        registerChangeHandler(isObjectType(OmniFocusUpdate.class), this::processOmniFocusProjects);
        registerChangeHandler(isObjectType(Project.class).and(isDelete()), this::processProjectDelete);
    }

    @Override
    public Set<Class<?>> consumedObjectTypes()
    {
        return Set.of(OmniFocusUpdate.class, Project.class);
    }

    @Override
    public Set<Class<?>> producedObjectTypes()
    {
        return Set.of(AttributeValue.class);
    }

    @Override
    protected boolean isFullRefreshRequired(Changelog changelog)
    {
        return false;
    }

    private Collection<Change<?>> processOmniFocusProjects(Change<?> change)
    {
        var changes = createChangeCollection();
        omniFocusRepository.projects().forEach(omniFocusProject ->
        {
            var project = projectRepository.projectsByName().get(omniFocusProject.name());
            if (project != null)
            {
                createUrl(changes, project, omniFocusProject);
                createPriority(changes, project, omniFocusProject);
                if (omniFocusProject.status() == ON_HOLD)
                {
                    createStatus(changes, project);
                }
                else
                {
                    deleteStatus(changes, project);
                }
            }
        });
        return changes;
    }

    private Collection<Change<?>> processProjectDelete(Change<?> change)
    {
        var changes = createChangeCollection();
        var project = change.objectAs(Project.class);
        deleteUrl(changes, project);
        deletePriority(changes, project);
        deleteStatus(changes, project);
        return changes;
    }

    private void createUrl(
        Collection<Change<?>> changes, Project project, OmniFocusProject omniFocusProject)
    {
        changes.add(create(
            new AttributeValue(
                project,
                urlAttribute,
                omniFocusProject.link(),
                WEIGHT
            ),
            AttributeValue.class
        ));
    }

    private void deleteUrl(Collection<Change<?>> changes, Project project)
    {
        changes.add(delete(
            new AttributeValue(
                project,
                urlAttribute,
                null,
                WEIGHT
            ),
            AttributeValue.class
        ));
    }

    private void createPriority(
        Collection<Change<?>> changes, Project project, OmniFocusProject omniFocusProject)
    {
        changes.add(create(
            new AttributeValue(
                project,
                priorityAttribute,
                omniFocusProject.priority(),
                WEIGHT
            ),
            AttributeValue.class
        ));
    }

    private void deletePriority(Collection<Change<?>> changes, Project project)
    {
        changes.add(delete(
            new AttributeValue(
                project,
                priorityAttribute,
                null,
                WEIGHT
            ),
            AttributeValue.class
        ));
    }

    private void createStatus(Collection<Change<?>> changes, Project project)
    {
        changes.add(create(
            new AttributeValue(
                project,
                statusAttribute,
                messages.projectOnHold(),
                WEIGHT
            ),
            AttributeValue.class
        ));
    }

    private void deleteStatus(Collection<Change<?>> changes, Project project)
    {
        changes.add(delete(
            new AttributeValue(
                project,
                statusAttribute,
                null,
                WEIGHT
            ),
            AttributeValue.class
        ));
    }
}
