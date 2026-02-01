package nl.ulso.vmc.personal;

import nl.ulso.curator.vault.Document;

import java.util.Optional;

public class Game
{
    private final Document document;
    private Integer rating;
    private String cover;

    public Game(Document document)
    {
        this.document = document;
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

    void setCover(String cover)
    {
        this.cover = cover;
    }

    void setRating(Integer rating)
    {
        this.rating = rating;
    }
}

