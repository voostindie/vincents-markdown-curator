package nl.ulso.vmc.personal;

import nl.ulso.markdown_curator.vault.Document;

record Author(Document document)
{
    public String name()
    {
        return document.name();
    }
}
