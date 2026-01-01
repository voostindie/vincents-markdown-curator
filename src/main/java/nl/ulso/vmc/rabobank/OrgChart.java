package nl.ulso.vmc.rabobank;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.markdown_curator.*;
import nl.ulso.markdown_curator.vault.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static java.util.Collections.emptyList;
import static java.util.regex.Pattern.compile;
import static java.util.regex.Pattern.quote;
import static java.util.stream.Collectors.toSet;

@Singleton
public class OrgChart
    extends DataModelTemplate
{
    public static final String TEAMS_FOLDER = "Teams";
    public static final String THIRD_PARTY_FOLDER = "3rd Parties";
    public static final String CONTACTS_FOLDER = "Contacts";

    private final Vault vault;
    private final Set<OrgUnit> orgUnits;

    @Inject
    public OrgChart(Vault vault)
    {
        this.vault = vault;
        this.orgUnits = ConcurrentHashMap.newKeySet();
    }

    @Override
    protected boolean isFullRefreshRequired(Changelog changelog)
    {
        return super.isFullRefreshRequired(changelog)
               || changelog.changes().anyMatch(hasObjectType(Document.class).and(isFolderInScope()));
    }

    @Override
    public Collection<Change<?>> fullRefresh()
    {
        orgUnits.clear();
        var teams = vault.folder(TEAMS_FOLDER).orElse(null);
        var contacts = vault.folder(CONTACTS_FOLDER).orElse(null);
        if (teams != null && contacts != null)
        {
            teams.documents().forEach(team -> team.accept(new OrgUnitFinder(teams, contacts)));
        }
        var thirdParties = vault.folder(THIRD_PARTY_FOLDER).orElse(null);
        if (thirdParties != null && contacts != null)
        {
            thirdParties.documents().forEach(
                thirdParty -> thirdParty.accept(new OrgUnitFinder(thirdParties, contacts)));
        }
        return emptyList();
    }

    private Predicate<? super Change<?>> isFolderInScope()
    {
        return change -> {
            var document = (Document) change.object();
            var folder = document.folder();
            return isFolderInScope(folder);
        };
    }

    private boolean isFolderInScope(Folder folder)
    {
        var topLevelFolderName = toplevelFolder(folder).name();
        return topLevelFolderName.contentEquals(CONTACTS_FOLDER) ||
               topLevelFolderName.contentEquals(TEAMS_FOLDER) ||
               topLevelFolderName.contentEquals(THIRD_PARTY_FOLDER);
    }

    private Folder toplevelFolder(Folder folder)
    {
        var toplevelFolder = folder;
        while (toplevelFolder != vault && toplevelFolder.parent() != vault)
        {
            toplevelFolder = toplevelFolder.parent();
        }
        return toplevelFolder;
    }

    List<OrgUnit> forParent(String parentTeamName)
    {
        return orgUnits.stream().filter(orgUnit -> orgUnit.parent()
            .map(parent -> parent.name().contentEquals(parentTeamName)).orElse(false)).toList();
    }

    List<OrgUnit> forContact(String contactName)
    {
        return orgUnits.stream().filter(orgUnit -> orgUnit.roles().values().stream()
            .anyMatch(roles -> roles.containsKey(contactName))).toList();
    }

    /**
     * Returns all contacts with a specific role in any of the specified teams or one of their
     * subteams.
     *
     * @param roleNames Substrings of roles to search for.
     * @param unitNames Substrings of unit names to search for.
     * @return All matching contacts.
     */
    List<Document> chapterFor(List<String> roleNames, List<String> unitNames)
    {
        var rolesSet = roleNames.stream().map(String::toLowerCase).collect(toSet());
        var unitNameSet = unitNames.stream().map(String::toLowerCase).collect(toSet());
        // TODO: protect against loops in the structure
        return orgUnits.stream()
            .filter(orgUnit -> isInHierarchy(orgUnit, unitNameSet))
            .flatMap(orgUnit -> orgUnit.roles().entrySet().stream()
                .filter(entry -> rolesSet.stream()
                    .anyMatch(entry.getKey().toLowerCase()::contains))
                .flatMap(entry -> entry.getValue().values().stream()))
            .sorted(Comparator.comparing(Document::name))
            .distinct()
            .toList();
    }

    private boolean isInHierarchy(OrgUnit unit, Set<String> unitNames)
    {
        var simpleName = unit.team().sortableTitle().toLowerCase();
        if (unitNames.stream().anyMatch(simpleName::contains))
        {
            return true;
        }
        return unit.parent()
            .map(parentDocument -> orgUnits.stream()
                .filter(orgUnit -> orgUnit.team() == parentDocument)
                .findFirst()
                .map(p -> isInHierarchy(p, unitNames))
                .orElse(false))
            .orElse(false);
    }

    private class OrgUnitFinder
        extends BreadthFirstVaultVisitor
    {
        private static final String CONTACTS_SECTION = "Contacts";
        private static final String ROLE_PATTERN_START = "^- (.*?): ";
        private final Folder orgUnitsFolder;
        private final Folder contactsFolder;

        private Document parent;
        private Map<String, Map<String, Document>> roles;

        OrgUnitFinder(Folder orgUnitsFolder, Folder contactsFolder)
        {
            this.orgUnitsFolder = orgUnitsFolder;
            this.contactsFolder = contactsFolder;
            this.parent = null;
        }

        @Override
        public void visit(Document document)
        {
            parent = null;
            if (document.fragments().size() > 2 &&
                document.fragments().get(1) instanceof TextBlock textBlock)
            {
                parent = textBlock.findInternalLinks().stream().map(InternalLink::targetDocument)
                    .map(orgUnitsFolder::document).filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst().orElse(null);
            }
            roles = new ConcurrentHashMap<>();
            super.visit(document);
            orgUnits.add(new OrgUnit(document, Optional.ofNullable(parent), roles));
        }

        @Override
        public void visit(Section section)
        {
            if (section.level() == 2 && (section.sortableTitle().contentEquals(CONTACTS_SECTION)))
            {
                super.visit(section);
            }
        }

        @Override
        public void visit(TextBlock textBlock)
        {
            String content = textBlock.markdown();
            textBlock.findInternalLinks().stream()
                .filter(link -> contactsFolder.document(link.targetDocument()).isPresent())
                .forEach(link -> {
                    var regex = compile(ROLE_PATTERN_START + quote(link.toMarkdown()),
                        Pattern.MULTILINE
                    );
                    var matcher = regex.matcher(content);
                    if (matcher.find())
                    {
                        var role = matcher.group(1);
                        var contact = contactsFolder.document(link.targetDocument());
                        contact.ifPresent(
                            c -> roles.computeIfAbsent(role, _ -> new HashMap<>())
                                .put(c.name(), c));
                    }
                });
        }
    }
}
