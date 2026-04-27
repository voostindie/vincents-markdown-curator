package nl.ulso.vmc.personal.reading;

import java.time.LocalDate;
import java.util.Optional;

public record BookReadingSession(Book book, ReadingSession session)
{
    public LocalDate fromDate()
    {
        return session.fromDate();
    }

    public Optional<LocalDate> toDate()
    {
        return session.toDate();
    }
}
