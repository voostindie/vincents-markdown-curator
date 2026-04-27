package nl.ulso.vmc.personal.reading;

import nl.ulso.curator.vault.Document;

import java.time.LocalDate;
import java.util.*;

final class MutableBook
    implements Book
{
    private final Document document;
    private final List<String> authors;
    private final List<ReadingSession> readingSessions;
    private Integer rating;
    private String cover;

    MutableBook(Document document)
    {
        this.document = document;
        this.authors = new ArrayList<>();
        this.readingSessions = new ArrayList<>();
    }

    @Override
    public Document document()
    {
        return document;
    }

    @Override
    public String name()
    {
        return document.name();
    }

    @Override
    public List<String> authorNames()
    {
        return Collections.unmodifiableList(authors);

    }

    void addAuthor(String authorName)
    {
        authors.add(authorName);
    }

    @Override
    public List<ReadingSession> readingSessions()
    {
        return Collections.unmodifiableList(readingSessions);
    }

    void addReadingSession(LocalDate start, Optional<LocalDate> end)
    {
        readingSessions.add(new ReadingSession(start, end));
    }

    void setRating(int rating)
    {
        this.rating = rating;
    }

    void setCover(String cover)
    {
        this.cover = cover;
    }

    @Override
    public Optional<String> cover()
    {
        return Optional.ofNullable(cover);
    }

    @Override
    public Optional<Integer> rating()
    {
        return Optional.ofNullable(rating);
    }
}
