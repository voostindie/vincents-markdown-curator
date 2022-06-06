package nl.ulso.vmc.personal;

import nl.ulso.markdown_curator.vault.Document;

public final class Author
{
    private final Document document;

    public Author(Document document)
    {
        this.document = document;
    }

    public Document document()
    {
        return document;
    }

    public String name()
    {
        return document.name();
    }
}
