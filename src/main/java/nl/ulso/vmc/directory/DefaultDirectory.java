package nl.ulso.vmc.directory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.change.ChangeHandler;
import nl.ulso.curator.change.ChangeProcessorTemplate;
import nl.ulso.curator.statistics.MeasurementCollector;
import nl.ulso.curator.statistics.MeasurementTracker;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Stream;

import static nl.ulso.curator.change.Change.isCreate;
import static nl.ulso.curator.change.Change.isDelete;
import static nl.ulso.curator.change.Change.isPayloadType;
import static nl.ulso.curator.change.Change.isUpdate;
import static nl.ulso.curator.change.ChangeHandler.newChangeHandler;
import static org.slf4j.LoggerFactory.getLogger;

@Singleton
final class DefaultDirectory
    extends ChangeProcessorTemplate
    implements Directory, MeasurementTracker
{
    private static final Logger LOGGER = getLogger(DefaultDirectory.class);

    private final ContactRepository contactRepository;
    private final Map<String, OrganizationalUnit> units;
    private final Set<Role> roles;

    @Inject
    DefaultDirectory(ContactRepository contactRepository)
    {
        this.contactRepository = contactRepository;
        this.units = new HashMap<>();
        this.roles = new HashSet<>();
    }

    @Override
    public Set<Class<?>> consumedPayloadTypes()
    {
        return Set.of(OrganizationalUnit.class, Role.class);
    }

    @Override
    public Set<Class<?>> requiredPayloadTypes()
    {
        return Set.of(ContactRepository.class);
    }

    @Override
    public void reset()
    {
        units.clear();
        roles.clear();
    }

    @Override
    protected List<? extends ChangeHandler> createChangeHandlers()
    {
        return List.of(
            newChangeHandler(
                isPayloadType(OrganizationalUnit.class).and(isCreate()),
                (change, _) -> {
                    var unit = change.as(OrganizationalUnit.class).newValue();
                    LOGGER.trace("New organizational unit: '{}'.", unit);
                    units.put(unit.name(), unit);
                }
            ),
            newChangeHandler(
                isPayloadType(OrganizationalUnit.class).and(isUpdate()),
                (change, _) -> {
                    var unit = change.as(OrganizationalUnit.class).newValue();
                    LOGGER.trace("Updated organizational unit: '{}'.", unit);
                    units.put(unit.name(), unit);
                }
            ),
            newChangeHandler(
                isPayloadType(OrganizationalUnit.class).and(isDelete()),
                (change, _) -> {
                    var unit = change.as(OrganizationalUnit.class).oldValue();
                    LOGGER.trace("Deleted organizational unit: '{}'.", unit);
                    units.remove(unit.name());
                }
            ),
            newChangeHandler(
                isPayloadType(Role.class).and(isCreate()),
                (change, _) -> {
                    var role = change.as(Role.class).newValue();
                    LOGGER.trace("New role: '{}'.", role);
                    roles.add(role);
                }
            ),
            newChangeHandler(
                isPayloadType(Role.class).and(isUpdate()),
                (_, _) ->
                    LOGGER.warn("Role updated detected. This is not supported. Ignoring.")
            ),
            newChangeHandler(
                isPayloadType(Role.class).and(isDelete()),
                (change, _) -> {
                    var role = change.as(Role.class).oldValue();
                    LOGGER.trace("Deleted role: '{}'.", role);
                    roles.remove(role);
                }
            )
        );
    }

    @Override
    public String name()
    {
        return Directory.class.getSimpleName();
    }

    @Override
    public void collectMeasurements(MeasurementCollector collector)
    {
        collector.total(OrganizationalUnit.class, units.size());
        collector.total(Role.class, roles.size());
    }

    @Override
    public Optional<Contact> contactNamed(String name)
    {
        return contactRepository.contactNamed(name);
    }

    @Override
    public Optional<OrganizationalUnit> organizationalUnitNamed(String name)
    {
        return Optional.ofNullable(units.get(name));
    }

    @Override
    public Stream<Role> allRolesFor(Contact contact)
    {
        return roles.stream()
            .filter(role -> role.contactName().contentEquals(contact.name()));
    }

    @Override
    public Stream<OrganizationalUnit> departmentsFor(OrganizationalUnit parent)
    {
        return units.values().stream()
            .filter(unit -> unit.parentName()
                .map(name -> name.contentEquals(parent.name()))
                .orElse(false));
    }

    @Override
    public Stream<Contact> contactsForRole(OrganizationalUnit unit, String roleDescription)
    {
        return roles.stream()
            .filter(role -> role.organizationalUnit().equals(unit))
            .filter(role -> role.description().contentEquals(roleDescription))
            .map(Role::contactName)
            .map(contactRepository::contactNamed)
            .filter(Optional::isPresent)
            .map(Optional::get);
    }
}
