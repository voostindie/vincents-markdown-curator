package nl.ulso.vmc.personal;

import nl.ulso.curator.vault.Document;

public record Author(Document document)
{
    public String name()
    {
        return document.name();
    }

    public String link()
    {
        return document.link();
    }
}
