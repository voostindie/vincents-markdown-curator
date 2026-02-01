package nl.ulso.vmc.graph;

import java.util.Optional;

/// Represents the shape of the node, as known in Mermaid.
///
/// <a href="https://mermaid.js.org/syntax/flowchart.html#expanded-node-shapes-in-mermaid-flowcharts-v11-3-0">Mermaid Docs</a>
public enum Shape
{
    NOTCHED_RECTANGLE("notch-rect"),
    HOURGLASS("hourglass"),
    LIGHTNING_BOLT("bolt"),
    CURLY_BRACE("brace"),
    CURLY_BRACE_R("brace_r"),
    CURLY_BRACES("braces"),
    LEAN_RIGHT("lean-r"),
    LEAN_LEFT("lean-l"),
    CYLINDER("cyl"),
    DIAMOND("diam"),
    HALF_ROUNDED_RECTANGLE("delay"),
    HORIZONTAL_CYLINDER("h-cyl"),
    LINED_CYLINDER("lin-cyl"),
    CURVED_TRAPEZOID("curv-trap"),
    DIVIDED_RECTANGLE("div-rect"),
    DOCUMENT("doc"),
    ROUNDED_RECTANGLE("rounded"),
    TRIANGLE("tri"),
    FILLED_RECTANGLE("fork"),
    WINDOW_PANE("win-pane"),
    FILLED_CIRCLE("f-circ"),
    LINED_DOCUMENT("lin-doc"),
    LINED_RECTANGLE("lin-rect"),
    TRAPEZOIDAL_PENTAGON("notch-pent"),
    FLIPPED_TRIANGLE("flip-tri"),
    SLOPED_RECTANGLE("sl-rect"),
    TRAPEZOID_BASE_TOP("trap-t"),
    STACKED_DOCUMENT("docs"),
    STACKED_RECTANGLE("st-rect"),
    ODD("odd"),
    FLAG("flag"),
    HEXAGON("hex"),
    TRAPEZOID_BASE_BOTTOM("trap-b"),
    RECTANGLE("rect"),
    CIRCLE("circle"),
    SMALL_CIRCLE("sm-circ"),
    DOUBLE_CIRCLE("dbl-circ"),
    FRAMED_CIRCLE("fr-circ"),
    BOW_TIE_RECTANGLE("bow-rect"),
    FRAMED_RECTANGLE("fr-rect"),
    CROSSED_CIRCLE("cross-circ"),
    TAGGED_DOCUMENT("tag-doc"),
    TAGGED_RECTANGLE("tag-rect"),
    STADIUM("stadium"),
    TEXT_BLOCK("text");

    private final String name;

    Shape(String name)
    {
        this.name = name;
    }

    public String toMermaidNode(String id, String label, Optional<String> className)
    {
        var classText = className.map(c -> ":::" + c).orElse("");
        if (this == RECTANGLE)
        {
            return id + classText + "@{label: \"" + label + "\"}";
        }
        return id + classText + "@{label: \"" + label + "\", shape: " + name + "}";
    }
}
