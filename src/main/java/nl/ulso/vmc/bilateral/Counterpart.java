package nl.ulso.vmc.bilateral;

import nl.ulso.curator.vault.Document;

/// Represents a counterpart in the bilateral meeting registry.
///
/// Currently, all settings are hardcoded. A counterpart is a document that:
/// - Exists in the [#CONTACTS_FOLDER] folder.
/// - Has a property [#BILATERAL_PROPERTY] present.
///
/// The default recurrence of a bilateral meeting is [#DEFAULT_RECURRENCE_IN_DAYS]. If
/// the value of the [#BILATERAL_PROPERTY] can be parsed as an integer, that value is
/// used instead, as a number of days between meetings.
public record Counterpart(Document document, int recurrenceInDays)
{
    static final String CONTACTS_FOLDER = "Contacts";
    static final String BILATERAL_PROPERTY = "1-on-1";
    static int DEFAULT_RECURRENCE_IN_DAYS = 14;

    public Counterpart(Document document)
    {
        this(
            document,
            document.frontMatter().integer(BILATERAL_PROPERTY, DEFAULT_RECURRENCE_IN_DAYS)
        );
    }

    public static boolean isCounterpart(Document document)
    {
        return document.folder().name().contentEquals(CONTACTS_FOLDER) &&
               document.frontMatter().hasProperty(BILATERAL_PROPERTY);
    }

    public String name()
    {
        return document.name();
    }

    public String link()
    {
        return document.link();
    }
}
