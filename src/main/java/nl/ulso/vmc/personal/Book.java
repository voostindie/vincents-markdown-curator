package nl.ulso.vmc.personal;

import nl.ulso.markdown_curator.vault.Document;

import java.util.*;

public final class Book
{
    private final Document document;
    private final List<Author> authors;
    private Integer rating;
    private String cover;

    public Book(Document document)
    {
        this.document = document;
        this.authors = new ArrayList<>();
    }

    public Document document()
    {
        return document;
    }

    public String name()
    {
        return document.name();
    }

    public List<Author> authors()
    {
        return authors;
    }

    void addAuthor(Author author)
    {
        authors.add(author);
    }

    void setRating(int rating)
    {
        this.rating = rating;
    }

    void setCover(String cover)
    {
        this.cover = cover;
    }

    public Optional<String> cover()
    {
        return Optional.ofNullable(cover);
    }

    public Optional<Integer> rating()
    {
        return Optional.ofNullable(rating);
    }
}
