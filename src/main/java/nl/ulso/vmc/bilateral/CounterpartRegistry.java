package nl.ulso.vmc.bilateral;

import java.util.Collection;

/// Keeps track of all counterparts for bilateral meetings.
public interface CounterpartRegistry
{
    Collection<Counterpart> counterparts();
}
