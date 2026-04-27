package nl.ulso.vmc.projectjournal;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.addon.journal.Marker;
import nl.ulso.curator.change.MapBasedEntityRepository;

@Singleton
final class DefaultProjectMarkerRepository
    extends MapBasedEntityRepository<Marker, String, ProjectMarker>
    implements ProjectMarkerRepository
{
    @Inject
    DefaultProjectMarkerRepository()
    {
    }

    @Override
    protected Class<Marker> sourceEntityClass()
    {
        return Marker.class;
    }

    @Override
    protected Class<ProjectMarker> targetEntityClass()
    {
        return ProjectMarker.class;
    }

    @Override
    protected boolean isEntity(Marker marker)
    {
        // Todo: figure out if the marker is a project marker.
        return false;
    }

    @Override
    protected ProjectMarker createEntityFrom(String name, Marker marker)
    {
        return new ProjectMarker(marker);
    }

    @Override
    protected String entityKeyFrom(Marker marker)
    {
        return marker.name();
    }
}
