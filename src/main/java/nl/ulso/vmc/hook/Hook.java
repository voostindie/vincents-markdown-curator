package nl.ulso.vmc.hook;

import java.util.Map;

public record Hook(String name, String address)
{
    private static final String HOOK_PROTOCOL = "hook://";
    private static final int HOOK_PROTOCOL_LENGTH = HOOK_PROTOCOL.length();
    private static final Map<String, String> PROTOCOL_TO_APPLICATION = Map.of(
            "https", "🌍",
            "http", "🌍",
            "ibooks", "📖",
            "ms-excel", "📈",
            "ms-powerpoint", "📽️",
            "ms-word", "📄",
            "omnifocus", "☑️",
            "file", "💾",
            "email", "✉️"
    );
    private static final String UNKNOWN_PROTOCOL = "❓";

    public String toMarkdown()
    {
        return resolveApplication() + " [" + name + "](" + fixHookmarkUrlEncoding(address) + ")";
    }

    private String resolveApplication()
    {
        var start = 0;
        var end = -1;
        if (address.startsWith(HOOK_PROTOCOL))
        {
            start = HOOK_PROTOCOL_LENGTH;
            end = address.indexOf("/", HOOK_PROTOCOL_LENGTH);
        }
        else
        {
            end = address.indexOf(':');
        }
        if (end == -1)
        {
            return UNKNOWN_PROTOCOL;
        }
        var protocol = address.substring(start, end);
        return PROTOCOL_TO_APPLICATION.getOrDefault(protocol, UNKNOWN_PROTOCOL);
    }

    /**
     * URLs from Hookmark seem to be encoded, but at least the character ")" is not, which
     * breaks the Markdown formatting.
     * @param url URL to format.
     * @return Properly encoded URL for use in Markdown links.
     */
    private String fixHookmarkUrlEncoding(String url)
    {
        return url.replace(")", "%29");
    }
}
