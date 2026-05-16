package nl.ulso.vmc.omnifocus;

import java.util.Collection;
import java.util.Optional;

public interface OmniFocusRepository
{
    Collection<OmniFocusProject> projects();

    Optional<OmniFocusProject> projectNamed(String name);
}
