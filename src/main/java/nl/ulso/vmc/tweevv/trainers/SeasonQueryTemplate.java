package nl.ulso.vmc.tweevv.trainers;

import nl.ulso.markdown_curator.query.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;

/**
 * Base class for queries that act on a specific season; the season is pre-selected, either pulled
 * from the document name if it matches the name of a season, and otherwise pulled from the
 * configuration parameter {@code season}.
 */
abstract class SeasonQueryTemplate
    implements Query
{
    private static final String LANGUAGE = "NL";
    private static final String CURRENCY = "EUR";

    private final TrainerModel       trainerModel;
    private final QueryResultFactory queryResultFactory;

    protected SeasonQueryTemplate(TrainerModel model, QueryResultFactory factory)
    {
        trainerModel = model;
        queryResultFactory = factory;
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return Map.of("season", "Season to get data for. Defaults to the current document.");
    }

    @Override
    public final QueryResult run(QueryDefinition definition)
    {
        var seasonName = definition.configuration().string("season", definition.document().name());
        var season = trainerModel.season(seasonName);
        if (season == null)
        {
            return queryResultFactory.error("No season found for '" + seasonName + "'");
        }
        return runFor(season, definition);
    }

    protected abstract QueryResult runFor(Season season, QueryDefinition definition);

    protected final QueryResultFactory queryResultFactory()
    {
        return queryResultFactory;
    }

    /**
     * @param amount Amount to format in Euros
     * @return The amount as a string in Euros, using the Dutch locale.
     */
    protected String toEuroString(BigDecimal amount)
    {
        var formatter = NumberFormat.getCurrencyInstance(Locale.of(LANGUAGE));
        formatter.setCurrency(Currency.getInstance(CURRENCY));
        return formatter.format(amount.doubleValue());
    }

    /**
     * @param number Number to format.
     * @return The number as a string in Euros, using the Dutch locale.
     */
    protected String toNumberString(BigDecimal number)
    {
        return NumberFormat.getInstance(Locale.of(LANGUAGE)).format(number.doubleValue());
    }

    /**
     * @param factor Factor to format.
     * @return The factor as a percentage, using the Dutch locale.
     */
    protected String toPercentageString(BigDecimal factor)
    {
        return NumberFormat.getPercentInstance(Locale.of(LANGUAGE)).format(factor.doubleValue());
    }
}
