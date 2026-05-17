package nl.ulso.vmc.directory;

import jakarta.inject.Inject;
import nl.ulso.curator.change.Changelog;
import nl.ulso.curator.query.*;

import java.util.*;

import static java.util.Comparator.comparing;

class RolesQuery
    implements Query
{
    private final Directory directory;
    private final QueryResultFactory queryResultFactory;

    @Inject
    public RolesQuery(Directory directory, QueryResultFactory queryResultFactory)
    {
        this.directory = directory;
        this.queryResultFactory = queryResultFactory;
    }

    @Override
    public String name()
    {
        return "roles";
    }

    @Override
    public String description()
    {
        return "lists all roles of a contact across all organizational units";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return Map.of("contact", "Name of the contact; defaults to the current document");
    }

    @Override
    public boolean isImpactedBy(Changelog changelog, QueryDefinition definition)
    {
        return resolveContact(definition).map(contact ->
            changelog.changesFor(Role.class).anyMatch(change ->
                change.value().contactName().contentEquals(contact.name())
            )
        ).orElse(false);
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        return resolveContact(definition).map(contact ->
        {
            var roles = directory.allRolesFor(contact)
                .sorted(comparing(role -> role.organizationalUnit().document().sortableTitle()))
                .map(role -> Map.of(
                    "Organizational Unit", role.organizationalUnit().document().link(),
                    "Role", role.description()
                ))
                .toList();
            return queryResultFactory.table(List.of("Organizational Unit", "Role"), roles);

        }).orElseGet(() ->
            queryResultFactory.error("Not a valid contact configured.")
        );
    }

    private Optional<Contact> resolveContact(QueryDefinition definition)
    {
        var contactName = definition.configuration()
            .string("contact", definition.document().name());
        return directory.contactNamed(contactName);
    }
}
