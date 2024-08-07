package nl.ulso.vmc.omnifocus;

import nl.ulso.markdown_curator.query.*;
import nl.ulso.markdown_curator.vault.Folder;
import nl.ulso.markdown_curator.vault.Vault;

import javax.inject.Inject;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.System.lineSeparator;
import static java.util.Collections.emptyMap;
import static java.util.ResourceBundle.getBundle;

public class OmniFocusQuery
        implements Query
{
    private final OmniFocusRepository omniFocusRepository;
    private final Vault vault;
    private final OmniFocusSettings settings;
    private final Locale locale;
    private final QueryResultFactory resultFactory;

    @Inject
    public OmniFocusQuery(
            Vault vault, OmniFocusRepository omniFocusRepository, OmniFocusSettings settings,
            Locale locale,
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
        return emptyMap();
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        return vault.folder(settings.projectFolder())
                .map(folder ->
                        (QueryResult) new OmniFocusQueryResult(
                                omniFocusRepository.projects(),
                                folder,
                                settings.includePredicate(), locale))
                .orElseGet(() -> resultFactory.error(
                        "Project folder not found: '" + settings.projectFolder() + "'"));
    }

    private static class OmniFocusQueryResult
            implements QueryResult
    {
        private final Collection<OmniFocusProject> omniFocusProjects;
        private final Folder projectFolder;
        private final Predicate<String> includePredicate;
        private final ResourceBundle bundle;

        OmniFocusQueryResult(
                Collection<OmniFocusProject> omniFocusProjects, Folder projectFolder,
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
                builder.append("### ")
                        .append(bundle.getString("missingPages.title"))
                        .append(lineSeparator())
                        .append(lineSeparator());
                missingPages.forEach(
                        name -> builder.append("- [[")
                                .append(name)
                                .append("]]")
                                .append(lineSeparator()));
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
                builder.append("### ")
                        .append(bundle.getString("missingProjects.title"))
                        .append(lineSeparator())
                        .append(lineSeparator());
                missingProjects.forEach(
                        document -> builder.append("- ")
                                .append(document.link())
                                .append(lineSeparator()));
                builder.append(lineSeparator());
            }
            if (missingPages.isEmpty() && missingProjects.isEmpty())
            {
                builder.append("**")
                        .append(bundle.getString("allGood.title"))
                        .append("**")
                        .append(lineSeparator())
                        .append(lineSeparator())
                        .append(bundle.getString("allGood.text"))
                        .append(lineSeparator());
            }
            return builder.toString().trim();
        }
    }
}
