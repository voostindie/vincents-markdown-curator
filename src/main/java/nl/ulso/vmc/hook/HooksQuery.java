package nl.ulso.vmc.hook;

import nl.ulso.markdown_curator.DocumentPathResolver;
import nl.ulso.markdown_curator.query.*;
import nl.ulso.markdown_curator.vault.Document;

import javax.inject.Inject;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Lists all hooks (bookmarks) for a document from Hook.
 * <p/>
 * If the current document hasn't been saved to disk recently, then the query doesn't run and
 * instead returns a no-op. Any existing output will remain. This is done because the number
 * of hook queries typically only grows, but only very few (the recent ones) have changing output.
 * <p/>
 * If the hooks in a document need to be updated even though the document doesn't require changes,
 * a simple `touch` is enough.
 */
public class HooksQuery
        implements Query
{
    private static final long MODIFICATION_THRESHOLD_MILLISECONDS = SECONDS.toMillis(10);
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
        return "Lists all hooks for the current document. This query runs only if the document " +
               "has been saved to disk in the last 10 seconds!";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return emptyMap();
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        if (isQueryDisabled(definition))
        {
            return resultFactory.noOp();
        }
        var documentUri = resolveUri(definition.document());
        return resultFactory.unorderedList(
                repository.listHooks(documentUri).stream().map(Hook::toMarkdown).toList());
    }

    private static boolean isQueryDisabled(QueryDefinition definition)
    {
        return definition.document().lastModified() + MODIFICATION_THRESHOLD_MILLISECONDS <
               System.currentTimeMillis();
    }

    private String resolveUri(Document document)
    {
        return documentPathResolver.resolveAbsolutePath(document).toUri().toString();
    }
}
