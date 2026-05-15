package nl.ulso.vmc.projectjournal;

import nl.ulso.curator.addon.journal.Marker;

/// Special [Marker] that is used to infer project statuses from the journal. The project status is
/// taken from the alias of the marker in the journal. The list of supported aliases is configured
/// in the `project-statuses` property.
final class ProjectStatusMarker
    extends ProjectMarker
{
    private static final String PROJECT_STATUSES_MARKER_PROPERTY = "project-statuses";

    ProjectStatusMarker(Marker marker)
    {
        super(marker);
    }

    public static boolean isProjectStatusMarker(Marker marker)
    {
        return marker.document().frontMatter().hasProperty(PROJECT_STATUSES_MARKER_PROPERTY);
    }

    @Override
    String frontMatterProperty()
    {
        return PROJECT_STATUSES_MARKER_PROPERTY;
    }

    /// The status of a project in the journal is the alias used for the marker.
    String resolveStatusFrom(String statusLink)
    {
        return markdownLinks().get(statusLink);
    }
}
