package nl.ulso.vmc.personal.gaming;

import nl.ulso.curator.vault.Document;

import java.util.Optional;

public final class Game
{
    private final Document document;
    private final Integer rating;
    private final String cover;

    Game(Document document, Integer rating, String cover)
    {
        this.document = document;
        this.rating = rating;
        this.cover = cover;
    }

    public Document document()
    {
        return document;
    }

    public Optional<String> cover()
    {
        return Optional.ofNullable(cover);
    }

    public Optional<Integer> rating()
    {
        return Optional.ofNullable(rating);
    }

    @Override
    public String toString()
    {
        return document.name();
    }
}
