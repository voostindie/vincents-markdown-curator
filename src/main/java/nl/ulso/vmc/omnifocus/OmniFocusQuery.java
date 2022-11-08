package nl.ulso.vmc.omnifocus;

import nl.ulso.markdown_curator.query.*;
import nl.ulso.markdown_curator.vault.Folder;
import nl.ulso.markdown_curator.vault.Vault;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.System.lineSeparator;
import static java.util.ResourceBundle.getBundle;

public class OmniFocusQuery
        implements Query
{
    private static final int DEFAULT_REFRESH_INTERVAL = 60000;

    private final OmniFocusRepository omniFocusRepository;
    private final Vault vault;
    private final OmniFocusSettings settings;
    private final Locale locale;
    private final QueryResultFactory resultFactory;

    @Inject
    public OmniFocusQuery(
            Vault vault, OmniFocusRepository omniFocusRepository, OmniFocusSettings settings, Locale locale,
            QueryResultFactory resultFactory)
    {
        this.resultFactory = resultFactory;
        this.omniFocusRepository = omniFocusRepository;
        this.vault = vault;
        this.settings = settings;
        this.locale = locale;
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
        return Map.of("refresh-interval",
                "Minimum number of milliseconds to wait for refreshes; defaults to 60000 (1 " +
                "minute)");
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        var refreshInterval =
                definition.configuration().integer("refresh-interval", DEFAULT_REFRESH_INTERVAL);
        return vault.folder(settings.projectFolder())
                .map(folder ->
                        (QueryResult) new OmniFocusQueryResult(
                                omniFocusRepository.projects(settings.omniFocusFolder(),
                                        refreshInterval),
                                folder,
                                settings.includePredicate(), locale))
                .orElseGet(() -> resultFactory.error(
                        "Project folder not found: '" + settings.projectFolder() + "'"));
    }

    private static class OmniFocusQueryResult
            implements QueryResult
    {
        private final List<OmniFocusProject> omniFocusProjects;
        private final Folder projectFolder;
        private final Predicate<String> includePredicate;
        private final ResourceBundle bundle;

        OmniFocusQueryResult(
                List<OmniFocusProject> omniFocusProjects, Folder projectFolder,
                Predicate<String> includePredicate, Locale locale)
        {
            this.omniFocusProjects = omniFocusProjects;
            this.projectFolder = projectFolder;
            this.includePredicate = includePredicate;
            this.bundle = getBundle("OmniFocus", locale);
        }

        @Override
        public String toMarkdown()
        {
            var builder = new StringBuilder();
            var missingPages = omniFocusProjects.stream()
                    .map(OmniFocusProject::name)
                    .filter(includePredicate)
                    .filter(name -> projectFolder.document(name).isEmpty())
                    .toList();
            if (!missingPages.isEmpty())
            {
                builder.append("### ");
                builder.append(bundle.getString("missingPages.title"));
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
                builder.append("### ");
                builder.append(bundle.getString("missingProjects.title"));
                builder.append(lineSeparator());
                builder.append(lineSeparator());
                missingProjects.forEach(
                        document -> builder.append("- ").append(document.link())
                                .append(lineSeparator()));
                builder.append(lineSeparator());
            }
            if (missingPages.isEmpty() && missingProjects.isEmpty())
            {
                builder.append("**");
                builder.append(bundle.getString("allGood.title"));
                builder.append("**");
                builder.append(lineSeparator());
                builder.append(lineSeparator());
                builder.append(bundle.getString("allGood.text"));
                builder.append(lineSeparator());
            }
            return builder.toString().trim();
        }
    }
}
