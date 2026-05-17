package nl.ulso.vmc.directory;

import jakarta.inject.Inject;
import nl.ulso.curator.change.Changelog;
import nl.ulso.curator.query.*;
import nl.ulso.curator.vault.Document;

import java.util.*;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;

class DepartmentsQuery
    implements Query
{
    private final Directory directory;
    private final QueryResultFactory resultFactory;

    @Inject
    public DepartmentsQuery(Directory directory, QueryResultFactory resultFactory)
    {
        this.directory = directory;
        this.resultFactory = resultFactory;
    }

    @Override
    public String name()
    {
        return "subteams";
    }

    @Override
    public String description()
    {
        return "Lists all departments of an organizational unit.";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return Map.of("style", "list or table; defaults to list"
            , "roles", "names of the roles to show in the table"
        );
    }

    @Override
    public boolean isImpactedBy(Changelog changelog, QueryDefinition definition)
    {
        return resolveOrganizationalUnit(definition)
            .map(OrganizationalUnit::name)
            .map(parentName ->
                isDepartmentAffected(changelog, parentName)
                || isRoleAffected(changelog, parentName)
            )
            .orElse(false);
    }

    private boolean isDepartmentAffected(Changelog changelog, String parentName)
    {
        return changelog.changesFor(OrganizationalUnit.class).anyMatch(change ->
            change.values().anyMatch(unit -> unit.parentName().map(name ->
                name.contentEquals(parentName)).orElse(false))
        );
    }

    private boolean isRoleAffected(Changelog changelog, String parentName)
    {
        return changelog.changesFor(Role.class).anyMatch(change ->
            change.values().anyMatch(role -> role.organizationalUnit().parentName().map(name ->
                name.contentEquals(parentName)).orElse(false))
        );
    }

    private Optional<OrganizationalUnit> resolveOrganizationalUnit(QueryDefinition definition)
    {
        return directory.organizationalUnitNamed(definition.document().name());
    }

    @Override
    public QueryResult run(QueryDefinition definition)
    {
        return resolveOrganizationalUnit(definition).map(parent ->
            {
                var style = definition.configuration().string("style", "list");
                switch (style)
                {
                    case "list" ->
                    {
                        var departments = directory.departmentsFor(parent)
                            .map(OrganizationalUnit::document)
                            .sorted(comparing(Document::sortableTitle))
                            .map(Document::link)
                            .toList();
                        return resultFactory.unorderedList(departments);
                    }
                    case "table" ->
                    {
                        var roles = definition.configuration().listOfStrings("roles");
                        var rows = directory.departmentsFor(parent)
                            .sorted(comparing(unit -> unit.document().sortableTitle()))
                            .map(unit ->
                            {
                                Map<String, String> row = new HashMap<>();
                                row.put("Department", unit.document().link());
                                for (String role : roles)
                                {
                                    row.put(role,
                                        directory.contactsForRole(unit, role)
                                            .map(Contact::document)
                                            .sorted(comparing(Document::sortableTitle))
                                            .map(Document::link)
                                            .collect(joining(", "))
                                    );
                                }
                                return row;

                            })
                            .toList();

                        var columns = new ArrayList<String>(roles.size() + 1);
                        columns.add("Department");
                        columns.addAll(roles);
                        return resultFactory.table(columns, rows);
                    }
                    default ->
                    {
                        return resultFactory.error("Unsupported style: " + style);
                    }
                }
            })
            .orElseGet(() -> resultFactory.error(
                "This document does not represent an organizational unit."));
    }
}
