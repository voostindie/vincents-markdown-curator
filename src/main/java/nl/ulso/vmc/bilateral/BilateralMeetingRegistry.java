package nl.ulso.vmc.bilateral;

import java.time.LocalDate;
import java.util.Map;

public interface BilateralMeetingRegistry
{
    String BILATERAL_PREFIX = "- 1-on-1 ";

    LocalDate FALLBACK_NEVER =
        LocalDate.of(1976, 11, 30);

    /// Constructs a map of all bilateral meetings, with the counterpart as the key and the date of
    /// the most recent meeting as the value. The map is sorted by date of meeting, oldest first.
    ///
    /// If the date of a meeting is [#FALLBACK_NEVER], there has not been a meeting yet.
    Map<Counterpart, LocalDate> resolveBilateralMeetings();
}
