package nl.ulso.vmc.tweevv;

import nl.ulso.markdown_curator.query.*;
import nl.ulso.markdown_curator.vault.*;

import java.util.*;

import static java.util.Comparator.comparing;
import static nl.ulso.markdown_curator.query.QueryResult.error;
import static nl.ulso.markdown_curator.query.QueryResult.table;

public class VolunteersQuery
        implements Query
{
    private final Vault vault;

    VolunteersQuery(Vault vault)
    {
        this.vault = vault;
    }

    @Override
    public String name()
    {
        return "volunteers";
    }

    @Override
    public String description()
    {
        return "Lists all volunteers for a specific season.";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return Map.of("season", "Season to list; e.g. '2021-2022'.");
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        var season = definition.configuration().string("season", null);
        if (season == null)
        {
            return error("Property 'season' is not specified");
        }
        return vault.folder("Contacten").map(contacts -> {
            var finder = new VolunteerFinder(season);
            contacts.accept(finder);
            var list = finder.volunteers.entrySet().stream()
                    .map(entry -> Map.of("Vrijwilliger", entry.getKey(),
                            "Taak", String.join(", ", entry.getValue())))
                    .sorted(comparing(map -> map.get("Vrijwilliger")))
                    .toList();
            return table(List.of("Vrijwilliger", "Taak"), list);
        }).orElse(error("Couldn't find folder 'Contacten'"));
    }

    public static class VolunteerFinder
            extends BreadthFirstVaultVisitor
    {

        private final String season;
        private boolean inSection;
        private final Map<String, List<String>> volunteers;

        public VolunteerFinder(String season)
        {
            this.season = season;
            this.volunteers = new HashMap<>();
            this.inSection = false;
        }

        @Override
        public void visit(Section section)
        {
            if (section.level() == 2 && section.title().contentEquals("Taken"))
            {
                inSection = true;
                super.visit(section);
                inSection = false;
            }
        }

        @Override
        public void visit(TextBlock textBlock)
        {
            if (!inSection)
            {
                return;
            }
            var lines = textBlock.lines();
            var size = lines.size();
            if (size < 3 || !lines.get(0).startsWith("| Seizoen"))
            {
                return;
            }
            for (int i = 2; i < size; i++)
            {
                var line = lines.get(i);
                String[] parts = line.split("\\|");
                if (parts.length < 3)
                {
                    continue;
                }
                if (parts[1].trim().contentEquals(season))
                {
                    var tasks = volunteers.computeIfAbsent(textBlock.document().link(),
                            link -> new ArrayList<>());
                    tasks.add(parts[2].trim());
                }
            }
        }
    }
}
