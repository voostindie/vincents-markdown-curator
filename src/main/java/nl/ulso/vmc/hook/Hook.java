package nl.ulso.vmc.hook;

public record Hook(String name, String address)
{
    public String toMarkdown()
    {
        return "[" + name + "](" + address + ")";
    }
}
