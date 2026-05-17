package nl.ulso.vmc.directory;

import java.util.Optional;

public interface TeamRepository
{
    Optional<Team> teamNamed(String name);
}
