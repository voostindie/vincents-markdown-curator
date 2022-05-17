package nl.ulso.vmc.rabobank;

import nl.ulso.markdown_curator.vault.Document;

import java.util.Map;
import java.util.Optional;

record OrgUnit(Document team, Optional<Document> parent, Map<String, Document> roles)
{
}
