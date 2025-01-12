package nl.ulso.vmc.graph;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents the data stored on edges between two nodes
 * <p/>
 * The data stored for each edge consists of the dates in the journal the edge definition was
 * found in. This is needed to be able to update the graph efficiently while changes to the
 * underlying repository happen.
 * <p/>
 * An edge that has no dates associated with it is invalid, and should be removed from the graph.
 */
class EdgeData
{
    private final Set<LocalDate> dates;

    EdgeData()
    {
        dates = new HashSet<>();
    }

    void addDate(LocalDate date)
    {
        dates.add(date);
    }

    void removeDate(LocalDate date)
    {
        dates.remove(date);
    }

    boolean isInvalid()
    {
        return dates.isEmpty();
    }
}
