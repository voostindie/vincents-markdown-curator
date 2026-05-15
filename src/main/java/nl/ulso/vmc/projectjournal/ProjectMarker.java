package nl.ulso.vmc.projectjournal;

import nl.ulso.curator.addon.journal.Marker;

import java.util.*;

/// Base class for markers used in the project journal to resolve attribute values from.
abstract class ProjectMarker
{
    private final Marker marker;
    private final Map<String, String> markdownLinks;

    ProjectMarker(Marker marker)
    {
        this.marker = marker;
        var map = new HashMap<String, String>();
        var aliases = marker.document().frontMatter().listOfStrings(frontMatterProperty());
        for (String alias : aliases)
        {
            var link = "[[" + marker.name() + "|" + alias + "]]";
            map.put(link, alias);
        }
        this.markdownLinks = Map.copyOf(map);
    }

    @Override
    public boolean equals(Object object)
    {
        if (object == null || getClass() != object.getClass())
        {
            return false;
        }
        ProjectMarker other = (ProjectMarker) object;
        return Objects.equals(marker, other.marker);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(marker);
    }

    protected String name()
    {
        return marker.name();
    }

    protected Map<String, String> markdownLinks()
    {
        return markdownLinks;
    }

    abstract String frontMatterProperty();

    @Override
    public String toString()
    {
        return marker.name();
    }
}

