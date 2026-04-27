package nl.ulso.vmc.personal.reading;

import nl.ulso.curator.vault.Document;

import java.util.List;
import java.util.Optional;

public interface Book
{
    Document document();

    String name();

    List<String> authorNames();

    List<ReadingSession> readingSessions();

    Optional<String> cover();

    Optional<Integer> rating();
}
