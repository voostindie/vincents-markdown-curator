package nl.ulso.vmc.project;

import nl.ulso.markdown_curator.vault.Document;

import java.time.LocalDate;

public final class Project
{
    private final String name;
    private final LocalDate lastModified;
    private final String leadWikiLink;
    private final Status status;

    Project(Document projectDocument, LocalDate lastModified)
    {
        this.name = projectDocument.name();
        this.lastModified = lastModified;
        this.leadWikiLink = projectDocument.frontMatter().string("lead", "");
        this.status = Status.fromString(projectDocument.frontMatter().string("status", ""));
    }

    public String name()
    {
        return name;
    }

    public LocalDate lastModified()
    {
        return lastModified;
    }

    public String leadWikiLink()
    {
        return leadWikiLink;
    }

    public Status status()
    {
        return status;
    }
}
