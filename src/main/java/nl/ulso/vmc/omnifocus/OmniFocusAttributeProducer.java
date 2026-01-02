package nl.ulso.vmc.omnifocus;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.markdown_curator.*;
import nl.ulso.markdown_curator.project.*;

import java.util.*;

import static nl.ulso.markdown_curator.Change.creation;
import static nl.ulso.markdown_curator.Change.deletion;
import static nl.ulso.markdown_curator.project.AttributeDefinition.PRIORITY;
import static nl.ulso.markdown_curator.project.AttributeDefinition.STATUS;
import static nl.ulso.vmc.omnifocus.Status.ON_HOLD;

/// Produces attribute values for projects from matching projects in OmniFocus.
///
/// 3 attributes are produced:
///
/// - Status, but only if the project is on hold in OmniFocus.
/// - Priority
/// - OmniFocus URL
@Singleton
public class OmniFocusAttributeProducer
    extends DataModelTemplate
{
    static final String OMNIFOCUS_URL_ATTRIBUTE = "omnifocus";
    private static final int WEIGHT = 200;

    private final Map<String, AttributeDefinition> attributeDefinitions;
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
        this.attributeDefinitions = attributeDefinitions;
        this.projectRepository = projectRepository;
        this.omniFocusRepository = omniFocusRepository;
        this.messages = messages;
        registerChangeHandler(hasObjectType(OmniFocus.class), this::processOmniFocusProjects);
        registerChangeHandler(hasObjectType(Project.class).and(isDeletion()), this::processProjectDelete);
    }

    @Override
    public Set<?> dependentModels()
    {
        return Set.of(projectRepository);
    }

    @Override
    public Set<Class<?>> consumedObjectTypes()
    {
        return Set.of(OmniFocus.class, Project.class);
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

    @Override
    public Collection<Change<?>> fullRefresh()
    {
        throw new IllegalStateException("This method should never be called!");
    }

    private Collection<Change<?>> processOmniFocusProjects(Change<?> change)
    {
        var changes = new ArrayList<Change<?>>();
        omniFocusRepository.projects().forEach(omniFocusProject ->
        {
            var project = projectRepository.projectsByName().get(omniFocusProject.name());
            if (project == null)
            {
                deleteUrl(changes, project);
                deletePriority(changes, project);
                deleteStatus(changes, project);
            }
            else
            {
                createUrl(changes, project, omniFocusProject);
                createPriority(omniFocusProject, changes, project);
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
        var changes = new ArrayList<Change<?>>();
        var project = change.objectAs(Project.class);
        deleteUrl(changes, project);
        deletePriority(changes, project);
        deleteStatus(changes, project);
        return changes;
    }

    private void createUrl(
        ArrayList<Change<?>> changes, Project project, OmniFocusProject omniFocusProject)
    {
        changes.add(creation(
            new AttributeValue(
                project,
                attributeDefinitions.get(OMNIFOCUS_URL_ATTRIBUTE),
                omniFocusProject.link(),
                WEIGHT
            ),
            AttributeValue.class
        ));
    }

    private void deleteUrl(ArrayList<Change<?>> changes, Project project)
    {
        changes.add(deletion(
            new AttributeValue(
                project,
                attributeDefinitions.get(OMNIFOCUS_URL_ATTRIBUTE),
                null,
                WEIGHT
            ),
            AttributeValue.class
        ));
    }

    private void createPriority(
        OmniFocusProject omniFocusProject, ArrayList<Change<?>> changes, Project project)
    {
        changes.add(creation(
            new AttributeValue(
                project,
                attributeDefinitions.get(PRIORITY),
                omniFocusProject.priority(),
                WEIGHT
            ),
            AttributeValue.class
        ));
    }

    private void deletePriority(ArrayList<Change<?>> changes, Project project)
    {
        changes.add(deletion(
            new AttributeValue(
                project,
                attributeDefinitions.get(PRIORITY),
                null,
                WEIGHT
            ),
            AttributeValue.class
        ));
    }

    private void createStatus(ArrayList<Change<?>> changes, Project project)
    {
        changes.add(creation(
            new AttributeValue(
                project,
                attributeDefinitions.get(STATUS),
                messages.projectOnHold(),
                WEIGHT
            ),
            AttributeValue.class
        ));
    }

    private void deleteStatus(ArrayList<Change<?>> changes, Project project)
    {
        changes.add(deletion(
            new AttributeValue(
                project,
                attributeDefinitions.get(STATUS),
                null,
                WEIGHT
            ),
            AttributeValue.class
        ));
    }
}
