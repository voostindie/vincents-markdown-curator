package nl.ulso.vmc.tweevv.trainers;

import nl.ulso.markdown_curator.query.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;

abstract class SeasonQueryTemplate
        implements Query
{
    private final TrainerModel trainerModel;
    private final QueryResultFactory queryResultFactory;

    protected SeasonQueryTemplate(TrainerModel model, QueryResultFactory factory)
    {
        trainerModel = model;
        queryResultFactory = factory;
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return Map.of("season", "Season to get information for. Defaults to the current document.");
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

    protected String toEuroString(BigDecimal amount)
    {
        return new DecimalFormat("'€'#,##0.00",
                DecimalFormatSymbols.getInstance(Locale.of("NL")))
                .format(amount.doubleValue());
    }

    protected String toNumberString(BigDecimal number)
    {
        return new DecimalFormat("#,###.##",
                DecimalFormatSymbols.getInstance(Locale.of("NL")))
                .format(number.doubleValue());
    }

    protected String toPercentageString(BigDecimal factor)
    {
        return toNumberString(factor.multiply(BigDecimal.valueOf(100))) + "%";
    }
}
