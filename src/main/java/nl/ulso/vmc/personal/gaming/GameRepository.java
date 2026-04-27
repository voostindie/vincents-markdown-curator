package nl.ulso.vmc.personal.gaming;

import java.util.Collection;

public interface GameRepository
{
    Collection<Game> findAll();
}
