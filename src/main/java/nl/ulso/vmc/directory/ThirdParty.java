package nl.ulso.vmc.directory;

import nl.ulso.curator.vault.Document;

public record ThirdParty(Document document)
{
    public String name()
    {
        return document.name();
    }

    @Override
    public String toString()
    {
        return document.name();
    }
}
