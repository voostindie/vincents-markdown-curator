package nl.ulso.vmc.directory;

import nl.ulso.curator.change.ChangeCollector;
import nl.ulso.curator.change.EntityProcessor;
import nl.ulso.curator.vault.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nl.ulso.curator.vault.InternalLinkFinder.extractInternalLinksFrom;

abstract class OrganizationalUnitProcessor<E>
    extends EntityProcessor<E>
{
    private final String contactsSection;

    OrganizationalUnitProcessor(DirectorySettings settings)
    {
        this.contactsSection = settings.contactsSection();
    }

    @Override
    public final Set<Class<?>> producedPayloadTypes()
    {
        return Set.of(OrganizationalUnit.class, Role.class);
    }

    protected final void entityCreated(E newEntity, ChangeCollector collector)
    {
        var temp = new TemporaryUnit(resolveDocumentFrom(newEntity));
        if (temp.isValid())
        {
            collector.create(temp.unit, OrganizationalUnit.class);
            temp.roles.forEach(role -> collector.create(role, Role.class));
        }
    }

    @Override
    protected final void entityUpdate(E oldEntity, E newEntity, ChangeCollector collector)
    {
        var oldTemp = new TemporaryUnit(resolveDocumentFrom(oldEntity));
        var newTemp = new TemporaryUnit(resolveDocumentFrom(newEntity));
        if (oldTemp.isValid() && !newTemp.isValid())
        {
            entityDeleted(oldEntity, collector);
        }
        else if (!oldTemp.isValid() && newTemp.isValid())
        {
            entityCreated(newEntity, collector);
        }
        else if (oldTemp.isValid() && newTemp.isValid())
        {
            collector.update(oldTemp.unit, newTemp.unit, OrganizationalUnit.class);
            var deletedRoles = new HashSet<>(oldTemp.roles);
            deletedRoles.removeAll(newTemp.roles);
            deletedRoles.forEach(role -> collector.delete(role, Role.class));
            var addedRoles = new HashSet<>(newTemp.roles);
            addedRoles.removeAll(oldTemp.roles);
            addedRoles.forEach(role -> collector.create(role, Role.class));
        }
    }

    @Override
    protected final void entityDeleted(E oldEntity, ChangeCollector collector)
    {
        var oldTemp = new TemporaryUnit(resolveDocumentFrom(oldEntity));
        if (oldTemp.isValid())
        {
            collector.delete(oldTemp.unit, OrganizationalUnit.class);
            oldTemp.roles.forEach(role -> collector.delete(role, Role.class));
        }
    }

    protected abstract Document resolveDocumentFrom(E entity);

    private class TemporaryUnit
    {
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private final Optional<String> parent;
        private final OrganizationalUnit unit;
        private final Set<Role> roles;

        TemporaryUnit(Document document)
        {
            this.parent = resolveParentDepartmentName(document);
            this.unit = new OrganizationalUnit(document, parent);
            this.roles = resolveContactRoles(unit);
        }

        boolean isValid()
        {
            return parent.isPresent() || !roles.isEmpty();
        }

        /// The parent department is the first link in the first text block of the document..
        private Optional<String> resolveParentDepartmentName(Document document)
        {
            if (document.fragments().size() > 2 &&
                document.fragments().get(1) instanceof TextBlock textBlock)
            {
                return textBlock.findInternalLinks().stream()
                    .map(InternalLink::targetDocument)
                    .findFirst();
            }
            return Optional.empty();

        }

        private Set<Role> resolveContactRoles(OrganizationalUnit unit)
        {
            var finder = new RolesFinder(unit);
            unit.document().accept(finder);
            return finder.roles;
        }
    }

    private class RolesFinder
        extends BreadthFirstVaultVisitor
    {
        private static final Pattern ROLE_PATTERN = Pattern.compile("^- (.*?):(.*?)$");

        private final OrganizationalUnit unit;
        private final Set<Role> roles;

        public RolesFinder(OrganizationalUnit unit)
        {
            this.unit = unit;
            this.roles = new HashSet<>();
        }

        @Override
        public void visit(Section section)
        {
            if (section.level() == 2 && (section.sortableTitle().contentEquals(contactsSection)))
            {
                super.visit(section);
            }
        }

        @Override
        public void visit(TextBlock textBlock)
        {
            textBlock.markdown().lines()
                .map(ROLE_PATTERN::matcher)
                .filter(Matcher::matches)
                .forEach(matcher ->
                {
                    var description = matcher.group(1);
                    var links = extractInternalLinksFrom(matcher.group(2));
                    if (!links.isEmpty())
                    {
                        var contactName = links.getFirst().targetDocument();
                        roles.add(new Role(unit, contactName, description));
                    }
                });
        }
    }
}
