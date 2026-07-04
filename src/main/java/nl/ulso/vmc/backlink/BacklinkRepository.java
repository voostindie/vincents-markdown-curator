package nl.ulso.vmc.backlink;

import java.util.Set;

interface BacklinkRepository
{
    Set<String> backlinksFor(String documentName);
}
