package nl.ulso.vmc.rabobank;

import nl.ulso.markdown_curator.query.*;
import nl.ulso.markdown_curator.vault.*;

import javax.inject.Inject;
import java.util.*;

import static java.util.Collections.emptyMap;
import static nl.ulso.markdown_curator.query.QueryResult.unorderedList;

class SystemsQuery
        implements Query
{
    private final Vault vault;

    @Inject
    SystemsQuery(Vault vault)
    {
        this.vault = vault;
    }

    @Override
    public String name()
    {
        return "systems";
    }

    @Override
    public String description()
    {
        return "outputs all systems in my scope in a table";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return emptyMap();
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        return vault.folder("Systems").map(folder ->
        {
            var finder = new SystemFinder();
            folder.accept(finder);
            var systems = finder.systems;
            Collections.sort(systems);
            return unorderedList(systems);
        }).orElse(QueryResult.error("Couldn't find the folder 'Systems'"));
    }

    private static class SystemFinder
            extends BreadthFirstVaultVisitor
    {
        private final List<String> systems = new ArrayList<>();

        @Override
        public void visit(Folder folder)
        {
            // Don't recurse into subfolders!
            if (folder.name().equals("Systems"))
            {
                super.visit(folder);
            }
        }

        @Override
        public void visit(Document document)
        {
            var domain = document.frontMatter().string("domain", null);
            if (domain != null)
            {
                systems.add(document.link());
            }
        }
    }
}
