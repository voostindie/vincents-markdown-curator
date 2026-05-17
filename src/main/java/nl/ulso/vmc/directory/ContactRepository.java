package nl.ulso.vmc.directory;

import java.util.Optional;

public interface ContactRepository
{
    Optional<Contact> contactNamed(String name);
}
