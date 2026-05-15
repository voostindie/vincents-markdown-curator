package nl.ulso.vmc.projectjournal;

import nl.ulso.curator.addon.journal.Marker;
import nl.ulso.curator.change.MapBasedEntityRepository;

import java.util.*;

abstract class ProjectMarkerRepository<P extends ProjectMarker>
    extends MapBasedEntityRepository<Marker, String, P>
{
    @Override
    protected final Class<Marker> sourceEntityClass()
    {
        return Marker.class;
    }

    @Override
    protected final boolean isEntity(Marker marker)
    {
        return isProjectMarker(marker);
    }

    @Override
    protected final String entityKeyFrom(Marker marker)
    {
        return marker.name();
    }

    @Override
    protected final P createEntityFrom(String documentName, Marker marker)
    {
        return createProjectMarker(marker);
    }

    @Override
    protected final Map<String, P> createMap()
    {
        return new HashMap<>(1);
    }

    protected abstract boolean isProjectMarker(Marker marker);

    protected abstract P createProjectMarker(Marker marker);

    final Set<String> allMarkers()
    {
        return map().keySet();
    }

    final P markerNamed(String name)
    {
        return map().get(name);
    }
}
