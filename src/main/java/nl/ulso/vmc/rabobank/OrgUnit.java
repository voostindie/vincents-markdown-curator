package nl.ulso.vmc.rabobank;

import nl.ulso.markdown_curator.vault.Document;

import java.util.Map;

record OrgUnit(Document team, Document parent, Map<String, Document> leadership)
{
}
