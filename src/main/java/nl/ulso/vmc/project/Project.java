package nl.ulso.vmc.project;

import nl.ulso.markdown_curator.vault.Document;
import nl.ulso.vmc.omnifocus.OmniFocusRepository;

import java.time.LocalDate;

import static nl.ulso.vmc.omnifocus.Status.ON_HOLD;

public final class Project
{
    private final String name;
    private final LocalDate lastModified;
    private final OmniFocusRepository omniFocusRepository;
    private final String lead;
    private final Status status;

    Project(
            Document projectDocument, LocalDate lastModified,
            OmniFocusRepository omniFocusRepository)
    {
        this.name = projectDocument.name();
        this.lastModified = lastModified;
        this.omniFocusRepository = omniFocusRepository;
        this.lead = fromWikiLink(projectDocument.frontMatter().string("lead", ""));
        this.status = Status.fromString(projectDocument.frontMatter().string("status", ""));
    }

    private String fromWikiLink(String link)
    {
        if (link.startsWith("[[") && link.endsWith("]]"))
        {
            return link.substring(2, link.length() - 2);
        }
        return link;
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
        return lead.isBlank() ? "" : "[[" + lead + "]]";
    }

    public Status status()
    {
        if (omniFocusRepository.statusOf(name) == ON_HOLD)
        {
            return Status.ON_HOLD;
        }
        return status;
    }
}
