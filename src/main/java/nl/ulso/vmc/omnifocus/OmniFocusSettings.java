package nl.ulso.vmc.omnifocus;

import java.util.List;

public record OmniFocusSettings(String projectFolder, String omniFocusFolder, List<String> ignoredProjects)
{
}
