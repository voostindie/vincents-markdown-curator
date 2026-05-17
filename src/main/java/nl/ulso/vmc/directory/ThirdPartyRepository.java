package nl.ulso.vmc.directory;

import java.util.Optional;

public interface ThirdPartyRepository
{
    Optional<ThirdParty> thirdPartyNamed(String name);
}
