package nl.ulso.vmc.omnifocus;

import jakarta.inject.Inject;
import nl.ulso.markdown_curator.project.Project;
import nl.ulso.markdown_curator.project.ProjectRepository;
import nl.ulso.markdown_curator.query.*;
import nl.ulso.markdown_curator.vault.Document;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.System.lineSeparator;
import static java.net.URLEncoder.encode;
import static java.util.Collections.emptyMap;

/**
 * Reports on inconsistencies between OmniFocus and the projects in this vault.
 */
public class OmniFocusQuery
        implements Query
{
    private final OmniFocusRepository omniFocusRepository;
    private final OmniFocusSettings settings;
    private final ProjectRepository projectRepository;
    private final OmniFocusMessages messages;

    @Inject
    public OmniFocusQuery(
            OmniFocusRepository omniFocusRepository, OmniFocusSettings settings,
            ProjectRepository projectRepository, OmniFocusMessages messages)
    {
        this.projectRepository = projectRepository;
        this.settings = settings;
        this.omniFocusRepository = omniFocusRepository;
        this.messages = messages;
    }

    @Override
    public String name()
    {
        return "omnifocus";
    }

    @Override
    public String description()
    {
        return "lists inconsistencies between OmniFocus and the projects in this vault";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return emptyMap();
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        var projectsWithoutDocuments = collectOmniFocusProjectsWithoutDocuments();
        var documentsWithoutProjects = collectDocumentsWithoutOmniFocusProjects();
        return new OmniFocusQueryResult(projectsWithoutDocuments, documentsWithoutProjects);
    }

    private List<OmniFocusProject> collectOmniFocusProjectsWithoutDocuments()
    {
        var documentNames = projectRepository.projectsByName().keySet();
        var omniFocusProjects = omniFocusRepository.projects();
        return omniFocusProjects.stream()
                .filter(project -> settings.includePredicate().test(project.name()))
                .filter(project -> !documentNames.contains(project.name()))
                .toList();
    }

    private List<Document> collectDocumentsWithoutOmniFocusProjects()
    {
        var omniFocusProjects = omniFocusRepository.projects().stream()
                .map(OmniFocusProject::name)
                .collect(Collectors.toSet());
        return projectRepository.projects().stream()
                .filter(project -> !omniFocusProjects.contains(project.name()))
                .map(Project::document)
                .toList();
    }

    private class OmniFocusQueryResult
            implements QueryResult
    {
        private final List<OmniFocusProject> projectsWithoutDocuments;
        private final List<Document> documentsWithoutProjects;

        OmniFocusQueryResult(
                List<OmniFocusProject> projectsWithoutDocuments,
                List<Document> documentsWithoutProjects)
        {
            this.projectsWithoutDocuments = projectsWithoutDocuments;
            this.documentsWithoutProjects = documentsWithoutProjects;
        }

        @Override
        public String toMarkdown()
        {
            var builder = new StringBuilder();
            if (projectsWithoutDocuments.isEmpty() && documentsWithoutProjects.isEmpty())
            {
                reportAllIsGood(builder);
            }
            else
            {
                if (!projectsWithoutDocuments.isEmpty())
                {
                    reportProjectsWithoutDocuments(builder);
                }
                if (!documentsWithoutProjects.isEmpty())
                {
                    reportDocumentsWithoutProjects(builder);
                }
            }
            return builder.toString().trim();
        }

        private void reportAllIsGood(StringBuilder builder)
        {
            builder.append("**")
                    .append(messages.allGoodTitle())
                    .append("**")
                    .append(lineSeparator())
                    .append(lineSeparator())
                    .append(messages.allGoodText())
                    .append(lineSeparator());
        }

        private void reportProjectsWithoutDocuments(StringBuilder builder)
        {
            builder.append("### ")
                    .append(messages.projectsWithoutDocumentsTitle())
                    .append(lineSeparator())
                    .append(lineSeparator());
            projectsWithoutDocuments.forEach(
                    project -> builder.append("- [[")
                            .append(project.name())
                            .append("]]")
                            .append(lineSeparator()));
            builder.append(lineSeparator());
        }

        private void reportDocumentsWithoutProjects(StringBuilder builder)
        {
            builder.append("### ")
                    .append(messages.documentsWithoutProjectsTitle())
                    .append(lineSeparator())
                    .append(lineSeparator());
            documentsWithoutProjects.forEach(
                    document -> builder.append("- ")
                            .append(document.link())
                            .append(" - [")
                            .append(messages.createProjectInOmniFocus())
                            .append("](omnifocus:///paste")
                            .append("?index=1")
                            .append("&target=/folder/")
                            .append(urlEncode(settings.omniFocusFolder()))
                            .append("&content=")
                            .append(urlEncode(document.name() + ":"))
                            .append(")")
                            .append(lineSeparator()));
            builder.append(lineSeparator());
        }

        private String urlEncode(String value)
        {
            return encode(value, StandardCharsets.UTF_8).replace("+", "%20");
        }
    }
}
