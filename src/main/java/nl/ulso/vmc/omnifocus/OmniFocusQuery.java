package nl.ulso.vmc.omnifocus;

import nl.ulso.markdown_curator.query.*;
import nl.ulso.markdown_curator.vault.Folder;
import nl.ulso.markdown_curator.vault.Vault;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.lineSeparator;
import static java.net.URLEncoder.encode;
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
                        (QueryResult) new OmniFocusQueryResult(folder))
                .orElseGet(() -> resultFactory.error(
                        "Project folder not found: '" + settings.projectFolder() + "'"));
    }

    private class OmniFocusQueryResult
            implements QueryResult
    {
        private final Folder projectFolder;
        private final ResourceBundle bundle;

        OmniFocusQueryResult(Folder projectFolder)
        {
            this.projectFolder = projectFolder;
            this.bundle = getBundle("OmniFocus", locale);
        }

        @Override
        public String toMarkdown()
        {
            var builder = new StringBuilder();
            var omniFocusProjects = omniFocusRepository.projects();
            var missingPages = omniFocusProjects.stream()
                    .map(OmniFocusProject::name)
                    .filter(settings.includePredicate())
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
                                .append(" [")
                                .append(bundle.getString("create.text"))
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

        private String urlEncode(String value)
        {
            return encode(value, StandardCharsets.UTF_8).replace("+", "%20");
        }
    }
}
