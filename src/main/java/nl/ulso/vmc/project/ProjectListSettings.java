package nl.ulso.vmc.project;

public record ProjectListSettings(String projectFolder, String timelineSection, String dateColumn,
                                  String projectColumn)
{
    public static final ProjectListSettings DUTCH =
            new ProjectListSettings("Projecten", "Activiteiten", "Datum", "Project");
    public static final ProjectListSettings ENGLISH =
            new ProjectListSettings("Projects", "Activities", "Date", "Project");
}
