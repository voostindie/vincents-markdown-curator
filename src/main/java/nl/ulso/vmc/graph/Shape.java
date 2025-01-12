package nl.ulso.vmc.graph;

/**
 * Represents the shape of the node, as known in Mermaid.
 * <p/>
 * TODO: Add more shapes.
 * TODO: Switch to the alternative format as soon as Obsidian 1.8.0 is out, with a newer Mermaid.
 */
public enum Shape
{
    RECTANGLE("[", "]"),
    ROUND("(", ")"),
    HEXAGON("{{", "}}");

    private final String prefix;
    private final String postfix;

    Shape(String prefix, String postfix)
    {
        this.prefix = prefix;
        this.postfix = postfix;
    }

    public String toMermaidNode(String id, String label)
    {
        return id + prefix + "\"" + label + "\"" + postfix;
    }
}
