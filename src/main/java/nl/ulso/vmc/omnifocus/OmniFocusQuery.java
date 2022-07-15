package nl.ulso.vmc.omnifocus;

import nl.ulso.markdown_curator.query.*;
import nl.ulso.markdown_curator.vault.Dictionary;
import nl.ulso.markdown_curator.vault.*;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.lineSeparator;
import static nl.ulso.markdown_curator.query.QueryResult.error;

public class OmniFocusQuery
        implements Query
{
    private static final String PROJECT_FOLDER = "project-folder";
    private static final String OMNIFOCUS_FOLDER = "omnifocus-folder";
    private static final String IGNORED_PROJECTS = "ignored-projects";
    private static final String REFRESH_INTERVAL = "refresh-interval";
    private static final String REFRESH_INTERVAL_TEXT =
            "Minimum number of milliseconds to wait for refreshes; defaults to 60000 (1 " +
            "minute)";
    private static final int DEFAULT_REFRESH_INTERVAL = 60000;

    private final OmniFocusRepository omniFocusRepository;
    private final Vault vault;
    private final OmniFocusSettings settings;

    public OmniFocusQuery(Vault vault)
    {
        this(vault, null);
    }

    @Inject
    public OmniFocusQuery(Vault vault, OmniFocusSettings settings)
    {
        this.omniFocusRepository = new OmniFocusRepository();
        this.vault = vault;
        this.settings = settings;
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
        if (settings != null)
        {
            return Map.of(
                    REFRESH_INTERVAL,
                    REFRESH_INTERVAL_TEXT
            );
        }
        return Map.of(
                PROJECT_FOLDER, "Folder in the vault where projects are stored",
                OMNIFOCUS_FOLDER, "OmniFocus folder to select projects from",
                IGNORED_PROJECTS, "list of OmniFocus projects to ignore; defaults to empty list",
                REFRESH_INTERVAL,
                REFRESH_INTERVAL_TEXT);
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        var configuration = configuration(definition);
        var projectFolder = configuration.string(PROJECT_FOLDER, null);
        if (projectFolder == null)
        {
            return error("Property '" + PROJECT_FOLDER + "' is missing.");
        }
        var omniFocusFolder = configuration.string(OMNIFOCUS_FOLDER, null);
        if (omniFocusFolder == null)
        {
            return error("Property '" + OMNIFOCUS_FOLDER + "' is missing.");
        }
        var ignoredProjects = new HashSet<>(configuration.listOfStrings(IGNORED_PROJECTS));
        var refreshInterval = configuration.integer(REFRESH_INTERVAL, DEFAULT_REFRESH_INTERVAL);
        return vault.folder(projectFolder)
                .map(folder ->
                        (QueryResult) new OmniFocusQueryResult(
                                omniFocusRepository.projects(omniFocusFolder, refreshInterval),
                                folder,
                                ignoredProjects))
                .orElse(error("Project folder not found: '" + projectFolder + "'"));
    }

    protected Dictionary configuration(QueryDefinition definition)
    {
        if (settings != null)
        {
            return Dictionary.mapDictionary(Map.of(
                    PROJECT_FOLDER, settings.projectFolder(),
                    OMNIFOCUS_FOLDER, settings.omniFocusFolder(),
                    IGNORED_PROJECTS, settings.ignoredProjects(),
                    REFRESH_INTERVAL,
                    definition.configuration().integer(REFRESH_INTERVAL, DEFAULT_REFRESH_INTERVAL)
            ));
        }
        return definition.configuration();
    }

    private static class OmniFocusQueryResult
            implements QueryResult
    {
        private final List<OmniFocusProject> omniFocusProjects;
        private final Folder projectFolder;
        private final Set<String> ignoredProjects;

        OmniFocusQueryResult(
                List<OmniFocusProject> omniFocusProjects, Folder projectFolder,
                Set<String> ignoredProjects)
        {
            this.omniFocusProjects = omniFocusProjects;
            this.projectFolder = projectFolder;
            this.ignoredProjects = ignoredProjects;
        }

        @Override
        public String toMarkdown()
        {
            var builder = new StringBuilder();
            var missingPages = omniFocusProjects.stream()
                    .map(OmniFocusProject::name)
                    .filter(name -> !ignoredProjects.contains(name))
                    .filter(name -> projectFolder.document(name).isEmpty())
                    .toList();
            if (!missingPages.isEmpty())
            {
                builder.append("### Projects without a matching page");
                builder.append(lineSeparator());
                builder.append(lineSeparator());
                missingPages.forEach(
                        name -> builder.append("- ").append(name).append(lineSeparator()));
                builder.append(lineSeparator());
            }
            var projectSet = omniFocusProjects.stream()
                    .map(OmniFocusProject::name)
                    .collect(Collectors.toSet());
            var missingProjects = projectFolder.documents().stream()
                    .filter(document -> !projectSet.contains(document.name()))
                    .toList();
            if (!missingProjects.isEmpty())
            {
                builder.append("### Pages without a matching project");
                builder.append(lineSeparator());
                builder.append(lineSeparator());
                missingProjects.forEach(
                        document -> builder.append("- ").append(document.link())
                                .append(lineSeparator()));
                builder.append(lineSeparator());
            }
            if (missingPages.isEmpty() && missingProjects.isEmpty())
            {
                builder.append("### All good!");
                builder.append(lineSeparator());
                builder.append(lineSeparator());
                builder.append("There are no inconsistencies to report.");
                builder.append(lineSeparator());
            }
            return builder.toString().trim();
        }
    }
}
