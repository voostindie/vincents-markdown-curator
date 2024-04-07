package nl.ulso.vmc.project;

public record ProjectListSettings(String projectFolder, String timelineSection,
                                  String dateColumn, String projectColumn, String leadColumn,
                                  String statusColumn)
{
}
