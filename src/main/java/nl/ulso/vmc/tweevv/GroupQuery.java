package nl.ulso.vmc.tweevv;

import nl.ulso.markdown_curator.query.*;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.Map;

public class GroupQuery
        implements Query
{
    private final VolunteeringModel model;
    private final QueryResultFactory resultFactory;

    @Inject
    public GroupQuery(VolunteeringModel model, QueryResultFactory resultFactory)
    {
        this.model = model;
        this.resultFactory = resultFactory;
    }

    @Override
    public String name()
    {
        return "group";
    }

    @Override
    public String description()
    {
        return "Lists all people in a group.";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return Map.of(
                "season", "Season to list the group for; defaults to the current season",
                "group", "Name of the group; defaults to the current document name"
        );
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        var seasonString = definition.configuration().string("season", currentSeason());
        var group = definition.configuration().string("group", definition.document().name());
        return VolunteeringModel.Season.fromString(seasonString).map(season ->
                {
                    var list = model.contactsFor(season, group).map(
                            VolunteeringModel.Contact::link).toList();
                    return resultFactory.unorderedList(list);
                }
        ).orElseGet(() -> resultFactory.error("Invalid season: " + seasonString));
    }

    private String currentSeason()
    {
        var date = LocalDate.now();
        var year = date.getYear();
        if (date.getMonth().getValue() < 9)
        {
            year--;
        }
        return year + "-" + (year + 1);
    }
}
