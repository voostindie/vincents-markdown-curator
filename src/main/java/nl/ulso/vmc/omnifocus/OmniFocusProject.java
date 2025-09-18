package nl.ulso.vmc.omnifocus;

import static nl.ulso.vmc.omnifocus.Status.UNKNOWN;

public record OmniFocusProject(String id, String name, Status status, int priority)
{
    static final OmniFocusProject NULL_PROJECT =
            new OmniFocusProject("null", "null", UNKNOWN, -1);

    public boolean exists()
    {
        return !id.contentEquals("null");
    }

    public String link()
    {
        return "omnifocus:///task/" + id;
    }
}
