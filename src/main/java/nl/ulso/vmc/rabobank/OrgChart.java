package nl.ulso.vmc.rabobank;

import nl.ulso.markdown_curator.DataModelTemplate;
import nl.ulso.markdown_curator.vault.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static java.util.regex.Pattern.quote;

@Singleton
public class OrgChart
        extends DataModelTemplate
{
    public static final String TEAMS_FOLDER = "Teams";
    public static final String CONTACTS_FOLDER = "Contacts";

    private final Vault vault;
    private final Set<OrgUnit> orgUnits;

    @Inject
    public OrgChart(Vault vault)
    {
        this.vault = vault;
        this.orgUnits = new HashSet<>();
    }

    @Override
    protected void fullRefresh()
    {
        orgUnits.clear();
        vault.folder(TEAMS_FOLDER).ifPresent(teams ->
                vault.folder(CONTACTS_FOLDER).ifPresent(contacts ->
                        teams.documents().forEach(team ->
                                team.accept(new OrgUnitFinder(teams, contacts)))));
    }

    List<OrgUnit> forParent(String parentTeamName)
    {
        return orgUnits.stream()
                .filter(orgUnit -> orgUnit.parent()
                        .map(parent -> parent.name().contentEquals(parentTeamName)).orElse(false))
                .toList();
    }

    List<OrgUnit> forContact(String contactName)
    {
        return orgUnits.stream()
                .filter(orgUnit -> orgUnit.roles().values().stream()
                        .anyMatch(roles -> roles.containsKey(contactName)))
                .toList();
    }


    private class OrgUnitFinder
            extends BreadthFirstVaultVisitor
    {
        private static final String ROLES_SECTION = "Roles";
        private static final String ROLE_PATTERN_START = "^- (.*?): ";
        private final Folder teams;
        private final Folder contacts;

        private Document parent;
        private Map<String, Map<String, Document>> roles;

        OrgUnitFinder(Folder teams, Folder contacts)
        {
            this.teams = teams;
            this.contacts = contacts;
            this.parent = null;
        }

        @Override
        public void visit(Document document)
        {
            parent = null;
            if (document.fragments().size() > 2
                && document.fragments().get(1) instanceof TextBlock textBlock)
            {
                parent = textBlock.findInternalLinks().stream()
                        .map(InternalLink::targetDocument)
                        .map(teams::document)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst()
                        .orElse(null);
            }
            roles = new HashMap<>();
            super.visit(document);
            orgUnits.add(new OrgUnit(document, Optional.ofNullable(parent), roles));
        }

        @Override
        public void visit(Section section)
        {
            if (section.level() == 2
                && section.sortableTitle().contentEquals(ROLES_SECTION))
            {
                super.visit(section);
            }
        }

        @Override
        public void visit(TextBlock textBlock)
        {
            String content = textBlock.content();
            textBlock.findInternalLinks().stream()
                    .filter(link -> contacts.document(link.targetDocument()).isPresent())
                    .forEach(link ->
                    {
                        var regex = compile(ROLE_PATTERN_START + quote(link.toMarkdown()),
                                Pattern.MULTILINE);
                        var matcher = regex.matcher(content);
                        if (matcher.find())
                        {
                            var role = matcher.group(1);
                            var contact = contacts.document(link.targetDocument());
                            contact.ifPresent(c ->
                                    roles.computeIfAbsent(role, r -> new HashMap<>())
                                            .put(c.name(), c));
                        }
                    });
        }
    }
}
