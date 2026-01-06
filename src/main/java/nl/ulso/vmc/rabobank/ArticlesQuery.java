package nl.ulso.vmc.rabobank;

import nl.ulso.markdown_curator.Changelog;
import nl.ulso.markdown_curator.query.*;
import nl.ulso.markdown_curator.vault.Dictionary;
import nl.ulso.markdown_curator.vault.*;

import jakarta.inject.Inject;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Pattern;

import static java.util.Collections.emptyMap;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static java.util.regex.Pattern.compile;
import static nl.ulso.markdown_curator.Change.isObjectType;

class ArticlesQuery
        implements Query
{
    private static final String CONFLUENCE = "https://confluence.dev.rabobank.nl";
    private final Vault vault;
    private final QueryResultFactory resultFactory;

    @Inject
    ArticlesQuery(Vault vault, QueryResultFactory resultFactory)
    {
        this.vault = vault;
        this.resultFactory = resultFactory;
    }

    @Override
    public String name()
    {
        return "articles";
    }

    @Override
    public String description()
    {
        return "outputs an overview of all articles";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return emptyMap();
    }

    @Override
    public boolean isImpactedBy(Changelog changelog, QueryDefinition definition)
    {
        return changelog.changes().anyMatch(isObjectType(Document.class)
            .and(change ->
                change.objectAs(Document.class).folder().name().equals("Articles"))
        );
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        return vault.folder("Articles").map(folder ->
        {
            var finder = new ArticleFinder();
            folder.accept(finder);
            var articles = finder.articles;
            articles.sort(comparing((Map<String, String> e) -> e.get("Date")).reversed());
            return resultFactory.table(List.of("Date", "Title", "Publication"), articles);
        }).orElseGet(() -> resultFactory.error("Couldn't find the folder 'Articles'"));
    }

    /**
     * For each article found, store a map with three values, all as a string: the date, the title
     * and the publication.
     * <p/>
     * The article date can be stored in on of two ways:
     * <ul>
     * <li>Inside a section called "Changes", in a bullet list. This one takes priority</li>
     * <li>Through the field "date" in the front matter.</li>
     * </ul>
     * An example "Changes" section:
     * <pre><code>
     *     ## Changes
     *
     *     - [[2024-12-15]]: Initial version.
     *     - [[2025-01-01]]: Processed review comments.
     * </code></pre>
     * <p/>
     * The order the dated entries are in is irrelevant. This finder picks the latest.
     * <p/>
     * The "publication" front matter field is just a bit of text. But with a if it starts with
     * "https://", then the text is turned into an actual link, with the label "Link". Additionally,
     * if the link points to Rabobank's Confluence site, the label is set to "Confluence".
     */
    private static class ArticleFinder
            extends BreadthFirstVaultVisitor
    {
        private static final Pattern CHANGES_PATTERN =
                compile("^- \\[\\[(\\d{4}-\\d{2}-\\d{2})]]: (.*)$");

        private final List<Map<String, String>> articles = new ArrayList<>();
        private String date;

        @Override
        public void visit(Document document)
        {
            date = null;
            super.visit(document);
            if (date == null)
            {
                date = document.frontMatter().date("date",
                        Instant.ofEpochMilli(document.lastModified())
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                ).toString();
            }
            articles.add(Map.of(
                    "Date", date,
                    "Title", document.link(),
                    "Publication", publicationLink(document.frontMatter())));
        }

        @Override
        public void visit(Section section)
        {
            if (section.level() == 2 && section.sortableTitle().contentEquals("Changes")
                && !section.fragments().isEmpty()
                && section.fragments().getFirst() instanceof TextBlock textBlock)
            {
                var lines = new ArrayList<>(textBlock.markdown().trim().lines().toList());
                if (lines.isEmpty())
                {
                    return;
                }
                lines.sort(reverseOrder());
                var mostRecent = lines.getFirst();
                var matcher = CHANGES_PATTERN.matcher(mostRecent);
                if (matcher.matches())
                {
                    date = matcher.group(1);
                }
            }
        }

        private String publicationLink(Dictionary frontMatter)
        {
            var link = frontMatter.string("publication", "Unpublished");
            var confluencePageId = frontMatter.integer("confluence-page-id", -1);
            if (confluencePageId != -1)
            {
                link = CONFLUENCE + "/pages/viewpage.action?pageId=" + confluencePageId;
            }
            if (link.startsWith("https://"))
            {
                var name = "Link";
                if (link.contains(CONFLUENCE))
                {
                    name = "Confluence";
                }
                return "[" + name + "](" + link + ")";
            }
            return link;
        }
    }
}
