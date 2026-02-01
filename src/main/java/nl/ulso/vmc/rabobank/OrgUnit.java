package nl.ulso.vmc.rabobank;

import nl.ulso.curator.vault.Document;

import java.util.*;

record OrgUnit(Document team, Optional<Document> parent, Map<String, Map<String, Document>> roles)
{
}
