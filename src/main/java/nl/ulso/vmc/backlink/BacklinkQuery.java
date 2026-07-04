package nl.ulso.vmc.backlink;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.Changelog;
import nl.ulso.curator.query.*;
import nl.ulso.emoji.EmojiStripper;

import java.util.Map;

import static java.util.Comparator.comparing;

@Singleton
final class BacklinkQuery
    implements Query
{
    static final String BACKLINK_QUERY_NAME = "backlinks";
    private static final String DOCUMENT_PROPERTY = "document";

    private final BacklinkRepository repository;
    private final QueryResultFactory queryResultFactory;

    @Inject
    BacklinkQuery(BacklinkRepository repository, QueryResultFactory queryResultFactory)
    {
        this.repository = repository;
        this.queryResultFactory = queryResultFactory;
    }

    @Override
    public String name()
    {
        return BACKLINK_QUERY_NAME;
    }

    static String resolveDocumentName(QueryDefinition definition)
    {
        return definition.configuration().string(
            DOCUMENT_PROPERTY,
            definition.document().name()
        );
    }

    @Override
    public String description()
    {
        return "Generates a list of documents that link to the specified document";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return Map.of(
            DOCUMENT_PROPERTY,
            "The document to generate backlinks for; defaults to the current document"
        );
    }

    @Override
    public boolean isImpactedBy(Changelog changelog, QueryDefinition definition)
    {
        var targetDocumentName = resolveDocumentName(definition);
        return changelog.changesFor(Backlink.class)
            .anyMatch(change ->
                change.value().targetDocumentName().contentEquals(targetDocumentName)
            );
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        var targetDocumentName = resolveDocumentName(definition);
        var links = repository.backlinksFor(targetDocumentName).stream()
            .sorted(comparing(EmojiStripper::stripEmojisFrom))
            .map(documentName -> "[[" + documentName + "]]")
            .toList();
        return queryResultFactory.unorderedList(links);
    }
}
