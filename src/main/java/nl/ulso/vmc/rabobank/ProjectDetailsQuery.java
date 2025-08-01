package nl.ulso.vmc.rabobank;

import jakarta.inject.Inject;
import nl.ulso.markdown_curator.project.Attribute;
import nl.ulso.markdown_curator.project.ProjectRepository;
import nl.ulso.markdown_curator.query.*;
import nl.ulso.markdown_curator.vault.Document;
import nl.ulso.vmc.omnifocus.OmniFocusRepository;

import java.util.Map;

public class ProjectDetailsQuery
        implements Query
{
    private final ProjectRepository projectRepository;
    private final OmniFocusRepository omniFocusRepository;
    private final QueryResultFactory queryResultFactory;
    private final GeneralMessages generalMessages;

    @Inject
    public ProjectDetailsQuery(
            ProjectRepository projectRepository, OmniFocusRepository omniFocusRepository,
            QueryResultFactory queryResultFactory,
            GeneralMessages generalMessages)
    {
        this.projectRepository = projectRepository;
        this.omniFocusRepository = omniFocusRepository;
        this.queryResultFactory = queryResultFactory;
        this.generalMessages = generalMessages;
    }

    @Override
    public String name()
    {
        return "projectdetails";
    }

    @Override
    public String description()
    {
        return "Generates an overview of a project attributes in a single line of text";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return Map.of("document",
                "Reference to the document that represents the project. Defaults to the current " +
                "document.");
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        var document = definition.configuration().string("document", definition.document().name());

        var project = projectRepository.projectsByName().get(document);
        if (project == null)
        {
            return queryResultFactory.empty();
        }
        var omniFocusProject = omniFocusRepository.project(project.name());
        return queryResultFactory.string(
                project.attributeValue(Attribute.STATUS)
                        .orElse(generalMessages.projectStatusUnknown())
                + " :: "
                + project.attributeValue(Attribute.LEAD).map(Document::link)
                        .orElse(generalMessages.projectLeadUnknown())
                + " :: "
                + project.attributeValue(Attribute.LAST_MODIFIED).map(d -> "[[" + d + "]]")
                        .orElse(generalMessages.projectDateUnknown())
                + " :: "
                + "[OmniFocus](" + omniFocusProject.link() + ")"
        );
    }
}
