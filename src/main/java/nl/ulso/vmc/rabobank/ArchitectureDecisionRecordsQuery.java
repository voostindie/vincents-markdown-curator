package nl.ulso.vmc.rabobank;

import nl.ulso.markdown_curator.Changelog;
import nl.ulso.markdown_curator.query.*;
import nl.ulso.markdown_curator.vault.*;

import jakarta.inject.Inject;
import java.util.*;
import java.util.regex.Pattern;

import static java.util.Collections.emptyMap;
import static java.util.Comparator.comparing;
import static java.util.regex.Pattern.compile;
import static nl.ulso.markdown_curator.Change.isObjectType;

class ArchitectureDecisionRecordsQuery
        implements Query
{
    private final Vault vault;
    private final QueryResultFactory resultFactory;

    @Inject
    ArchitectureDecisionRecordsQuery(Vault vault, QueryResultFactory resultFactory)
    {
        this.vault = vault;
        this.resultFactory = resultFactory;
    }

    @Override
    public String name()
    {
        return "adrs";
    }

    @Override
    public String description()
    {
        return "outputs an overview of all ADRs";
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
                change.as(Document.class).object().folder().name().equals("ADRs"))
        );
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        return vault.folder("ADRs").map(folder ->
        {
            var finder = new AdrFinder();
            folder.accept(finder);
            var adrs = finder.adrs;
            adrs.sort(comparing((Map<String, String> e) -> e.get("ID")));
            return resultFactory.table(List.of("ID", "Date", "Status", "Name"), adrs);
        }).orElseGet(() -> resultFactory.error("Couldn't find the folder 'ADRs'"));
    }

    private static class AdrFinder
            extends BreadthFirstVaultVisitor
    {
        private static final Pattern CHANGES_PATTERN =
                compile("^- \\[\\[(\\d{4}-\\d{2}-\\d{2})]]: (.*)$");

        private final List<Map<String, String>> adrs = new ArrayList<>();

        private String date;
        private String status;

        @Override
        public void visit(Document document)
        {
            var id = document.frontMatter().string("id", null);
            if (id == null)
            {
                return;
            }
            date = "";
            status = "";
            super.visit(document);
            adrs.add(Map.of(
                    "ID", id,
                    "Date", date,
                    "Status", status,
                    "Name", document.link()
            ));
        }

        @Override
        public void visit(Section section)
        {
            if (section.level() == 2 && !section.fragments().isEmpty()
                && section.fragments().getFirst() instanceof TextBlock textBlock)
            {
                var title = section.sortableTitle();
                if (title.contentEquals("Changes"))
                {
                    var lines = new ArrayList<>(textBlock.markdown().trim().lines().toList());
                    if (lines.isEmpty())
                    {
                        return;
                    }
                    lines.sort(Comparator.reverseOrder());
                    var mostRecent = lines.getFirst();
                    var matcher = CHANGES_PATTERN.matcher(mostRecent);
                    if (matcher.matches())
                    {
                        date = matcher.group(1);
                    }
                }
                else if (title.contentEquals("Status"))
                {
                    var lines = textBlock.markdown().trim().lines().toList();
                    if (!lines.isEmpty())
                    {
                        status = lines.getFirst();
                    }
                }
            }
        }
    }
}
