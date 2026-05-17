package nl.ulso.vmc.directory;

import nl.ulso.curator.vault.Document;

import java.util.Objects;
import java.util.Optional;

// An organizational unit is a [Team] or a [ThirdParty] that has a list of [Contacts] registered,
// or that has a parent organizational unit defined.
public record OrganizationalUnit(Document document, Optional<String> parentName)
{
    public String name()
    {
        return document.name();
    }

    @Override
    public boolean equals(Object object)
    {
        if (object == null || getClass() != object.getClass())
        {
            return false;
        }
        OrganizationalUnit other = (OrganizationalUnit) object;
        return Objects.equals(document.name(), other.document.name());
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(document.name());
    }

    @Override
    public String toString()
    {
        return document.name();
    }
}
