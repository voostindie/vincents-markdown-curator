package nl.ulso.vmc.tweevv.volunteers;

import nl.ulso.curator.vault.Document;

public record Contact(Document document)
{
    public String name()
    {
        return document.name();
    }

    public String link()
    {
        return document.link();
    }

    @Override
    public String toString()
    {
        return name();
    }
}
