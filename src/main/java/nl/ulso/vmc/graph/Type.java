package nl.ulso.vmc.graph;

/**
 * Represents the type of the nodes; linked to the subfolder within the root of the repository.
 * <p/>
 * Folders that do not have a type are not converted to nodes.
 * <p/>
 * Every node type has a classifier attached. This classifier is used when rendering individual
 * nodes. This allows custom code to change colors, strokes, fonts and so on of each node.
 *
 * @param typeName   Name of the type.
 * @param folderName Name of the folder under the root that holds this type of node.
 * @param shape      Shape nodes of this type must have when rendered to Mermaid.
 * @param classifier Resolver for Mermaid class, for individual nodes.
 */
public record Type(String typeName, String folderName, Shape shape,
                   MermaidNodeClassifier classifier)
{
    public Type(String typeName, String folderName, Shape shape)
    {
        this(typeName, folderName, shape, MermaidNodeClassifier.DEFAULT_CLASSIFIER);
    }
}
