package nl.ulso.vmc.rabobank;

import nl.ulso.markdown_curator.query.*;
import nl.ulso.markdown_curator.vault.Dictionary;
import nl.ulso.markdown_curator.vault.*;

import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Pattern;

import static java.util.Collections.emptyMap;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static java.util.regex.Pattern.compile;

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
                date = Instant.ofEpochMilli(document.lastModified())
                        .atZone(ZoneId.of("UTC")).toLocalDate().toString();
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
                && section.fragments().size() > 0
                && section.fragments().get(0) instanceof TextBlock textBlock)
            {
                var lines = new ArrayList<>(textBlock.lines());
                if (lines.isEmpty())
                {
                    return;
                }
                lines.sort(reverseOrder());
                var mostRecent = lines.get(0);
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
