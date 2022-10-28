package nl.ulso.vmc.hook;

import nl.ulso.markdown_curator.DocumentPathResolver;
import nl.ulso.markdown_curator.query.*;
import nl.ulso.markdown_curator.vault.Document;

import javax.inject.Inject;
import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * Lists all hooks (bookmarks) for a document from Hook.
 * <p/>
 * If the current document is archived, then the query doesn't run and instead returns a no-op.
 * Any existing output will remain. This is to ensure that old, archived notes don't end up
 * with an empty list of hooks; the list of hooks is essentially frozen on archival.
 */
public class HooksQuery
        implements Query
{
    private static final String ARCHIVE_PATH = "/_Archive/";
    private final HookmarkRepository repository;
    private final DocumentPathResolver documentPathResolver;
    private final QueryResultFactory resultFactory;

    @Inject
    public HooksQuery(
            HookmarkRepository repository, DocumentPathResolver documentPathResolver,
            QueryResultFactory resultFactory)
    {
        this.repository = repository;
        this.documentPathResolver = documentPathResolver;
        this.resultFactory = resultFactory;
    }

    @Override
    public String name()
    {
        return "hooks";
    }

    @Override
    public String description()
    {
        return "Lists all hooks for the current document";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return emptyMap();
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        var documentUri = resolveUri(definition.document());
        if (isDocumentArchived(documentUri))
        {
            return resultFactory.noOp();
        }
        return resultFactory.unorderedList(
                repository.listHooks(documentUri).stream().map(Hook::toMarkdown).toList());
    }

    private String resolveUri(Document document)
    {
        return documentPathResolver.resolveAbsolutePath(document).toUri().toString();
    }

    private boolean isDocumentArchived(String documentUri)
    {
        return documentUri.contains(ARCHIVE_PATH);
    }
}
