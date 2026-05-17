package nl.ulso.vmc.directory;

public record DirectorySettings(
    String teamsFolder,
    String contactsFolder,
    String thirdPartiesFolder,
    String contactsSection)
{
}
