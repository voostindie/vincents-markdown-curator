package nl.ulso.vmc.graph;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.Changelog;
import nl.ulso.curator.query.*;

import java.util.Map;

@Singleton
final class DisabledGraphQuery
    implements Query
{
    private final QueryResultFactory queryResultFactory;

    @Inject
    DisabledGraphQuery(QueryResultFactory queryResultFactory)
    {
        this.queryResultFactory = queryResultFactory;
    }

    @Override
    public String name()
    {
        return "graph";
    }

    @Override
    public String description()
    {
        return "DISABLED QUERY.";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return Map.of();
    }

    @Override
    public boolean isImpactedBy(Changelog changelog, QueryDefinition definition)
    {
        return false;
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        return queryResultFactory.string(
            "*(The graph query is currently disabled. Apologies to me for the inconvenience I've" +
            " brought myself!)*");
    }
}
