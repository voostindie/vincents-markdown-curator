package nl.ulso.vmc.project;

public enum Status
{
    GREEN,
    AMBER,
    RED,
    ON_HOLD,
    UNKNOWN;

    public static Status fromString(String status)
    {
        if (status.equalsIgnoreCase("green") || status.contentEquals("🟢"))
        {
            return GREEN;
        }
        else if (status.equalsIgnoreCase("amber") || status.contentEquals("🟠"))
        {
            return AMBER;
        }
        else if (status.equalsIgnoreCase("red") || status.contentEquals("🔴"))
        {
            return RED;
        }
        else if (status.equalsIgnoreCase("on hold") || status.contentEquals("⭕️"))
        {
            return ON_HOLD;
        }
        return UNKNOWN;
    }

    public String toMarkdown()
    {
        return switch (this)
        {
            case GREEN -> "🟢";
            case AMBER -> "🟠";
            case RED -> "🔴";
            case ON_HOLD -> "⭕️";
            case UNKNOWN -> "⚪️";
        };
    }
}
