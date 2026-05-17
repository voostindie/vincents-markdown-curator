package nl.ulso.vmc.directory;

import java.util.Optional;
import java.util.stream.Stream;

public interface Directory
{
    Optional<Contact> contactNamed(String name);

    Optional<OrganizationalUnit> organizationalUnitNamed(String name);

    Stream<Role> allRolesFor(Contact contact);

    Stream<OrganizationalUnit> departmentsFor(OrganizationalUnit parent);

    Stream<Contact> contactsForRole(OrganizationalUnit unit, String roleDescription);
}
