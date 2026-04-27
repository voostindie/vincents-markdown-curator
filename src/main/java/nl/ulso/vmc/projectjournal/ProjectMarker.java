package nl.ulso.vmc.projectjournal;

import nl.ulso.curator.addon.journal.Marker;

/// Special [[Marker]] that is used to infer project attributes from the journal.
public record ProjectMarker(Marker marker)
{
}
