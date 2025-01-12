package nl.ulso.vmc.graph;

import java.util.*;

import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toSet;

public final class MermaidGraphSettings
{
    private final Set<Type> types;
    private final Map<String, String> mermaidClassDefinitions;
    private final Set<String> folderNames;

    public MermaidGraphSettings(Set<Type> types)
    {
        this.types = types;
        var map = new TreeMap<String, String>();
        var classifiers = types.stream().map(Type::classifier).collect(toSet());
        for (MermaidNodeClassifier classifier : classifiers)
        {
            map.putAll(classifier.classDefinitions());
        }
        this.mermaidClassDefinitions = unmodifiableMap(map);
        this.folderNames = types.stream().map(Type::folderName).collect(toSet());
    }

    public Set<Type> nodeTypes()
    {
        return types;
    }

    public Set<String> folderNames()
    {
        return folderNames;
    }

    public Map<String, String> mermaidClassDefinitions()
    {
        return mermaidClassDefinitions;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass())
        {
            return false;
        }
        var that = (MermaidGraphSettings) obj;
        return Objects.equals(this.types, that.types);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(types);
    }
}
