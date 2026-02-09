package nl.ulso.vmc.personal;

import jakarta.inject.Inject;
import nl.ulso.curator.change.Changelog;
import nl.ulso.curator.query.*;

import java.util.*;

import static java.util.Comparator.reverseOrder;
import static nl.ulso.curator.change.Change.isPayloadType;

public class ArticleQuery
    implements Query
{
    private final ArticleRepository articleRepository;
    private final QueryResultFactory queryResultFactory;

    @Inject
    public ArticleQuery(ArticleRepository articleRepository, QueryResultFactory queryResultFactory)
    {
        this.articleRepository = articleRepository;
        this.queryResultFactory = queryResultFactory;
    }

    @Override
    public String name()
    {
        return "articles";
    }

    @Override
    public String description()
    {
        return "Generates an overview of all articles as a table, reverse ordered on date";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return Collections.emptyMap();
    }

    @Override
    public boolean isImpactedBy(Changelog changelog, QueryDefinition definition)
    {
        return changelog.changes().anyMatch(isPayloadType(ArticleRepositoryUpdate.class));
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        return queryResultFactory.table(
            List.of("Date", "Title"),
            articleRepository.articles()
                .sorted(reverseOrder())
                .map(article -> Map.of(
                    "Date", article.dateLink(),
                    "Title", article.link()
                ))
                .toList()
        );
    }
}
