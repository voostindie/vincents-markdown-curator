package nl.ulso.vmc.directory;

import java.util.Objects;

public record Role(OrganizationalUnit organizationalUnit, String contactName, String description)
{
    @Override
    public boolean equals(Object object)
    {
        if (object == null || getClass() != object.getClass())
        {
            return false;
        }
        Role other = (Role) object;
        return Objects.equals(contactName, other.contactName) &&
               Objects.equals(description, other.description) &&
               Objects.equals(organizationalUnit.name(), other.organizationalUnit.name());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(organizationalUnit.name(), contactName, description);
    }

    @Override
    public String toString()
    {
        return organizationalUnit.name() + "', '" + contactName + "', '" + description;
    }
}
