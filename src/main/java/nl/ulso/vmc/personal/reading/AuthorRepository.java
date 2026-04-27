package nl.ulso.vmc.personal.reading;

import java.util.Optional;

public interface AuthorRepository
{
    Optional<Author> findByName(String name);
}
