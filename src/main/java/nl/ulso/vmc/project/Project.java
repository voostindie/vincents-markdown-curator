package nl.ulso.vmc.project;

import nl.ulso.markdown_curator.vault.Document;
import nl.ulso.vmc.omnifocus.OmniFocusRepository;

import java.time.LocalDate;

public final class Project
{
    private final String name;
    private final LocalDate lastModified;
    private final OmniFocusRepository omniFocusRepository;
    private final String leadWikiLink;
    private final Status status;

    Project(Document projectDocument, LocalDate lastModified, OmniFocusRepository omniFocusRepository)
    {
        this.name = projectDocument.name();
        this.lastModified = lastModified;
        this.omniFocusRepository = omniFocusRepository;
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

    public int priority() {return omniFocusRepository.priorityOf(name);}

    public String leadWikiLink()
    {
        return leadWikiLink;
    }

    public Status status()
    {
        return status;
    }
}
