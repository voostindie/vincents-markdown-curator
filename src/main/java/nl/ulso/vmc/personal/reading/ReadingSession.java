package nl.ulso.vmc.personal.reading;

import java.time.LocalDate;
import java.util.Optional;

public record ReadingSession(LocalDate fromDate, Optional<LocalDate> toDate)
{
}
