package nl.ulso.vmc.bilateral;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import nl.ulso.curator.RunMode;
import nl.ulso.curator.change.ExternalChangeHandler;
import org.slf4j.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledExecutorService;

import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static nl.ulso.vmc.bilateral.Morning.MORNING;
import static org.slf4j.MDC.getCopyOfContextMap;

@Singleton
final class MorningAlarm
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MorningAlarm.class);

    private final ScheduledExecutorService alarmClock;

    @Inject
    public MorningAlarm(ExternalChangeHandler externalChangeHandler)
    {
        this.alarmClock = newSingleThreadScheduledExecutor();
        switch (RunMode.get())
        {
            case DAEMON -> scheduleDailyMorningAlarm(externalChangeHandler);
            case ONCE ->
            {
                // Do nothing. In a one-off run mode, the alarm is not needed.
            }
            default -> throw new IllegalArgumentException("Unsupported run mode: " + RunMode.get());
        }
    }

    private void scheduleDailyMorningAlarm(ExternalChangeHandler externalChangeHandler)
    {
        var contextMap = getCopyOfContextMap();
        var task = alarmClock.scheduleAtFixedRate(() ->
            {
                MDC.setContextMap(contextMap);
                LOGGER.info("Good morning! Publishing an event for the start of a new day.");
                externalChangeHandler.process(MORNING);
            },
            secondsUntilNextMorning(),
            SECONDS.convert(1, DAYS),
            SECONDS
        );
        getRuntime().addShutdownHook(
            new Thread(() ->
            {
                MDC.setContextMap(contextMap);
                LOGGER.debug("Shutting down the daily morning alarm.");
                task.cancel(true);
            })
        );

    }

    private long secondsUntilNextMorning()
    {
        var tomorrowAtSeven = LocalDate.now().atTime(7, 0).plusDays(1);
        var initialDelay = LocalDateTime.now().until(tomorrowAtSeven, ChronoUnit.SECONDS);
        LOGGER.debug("Scheduling the first morning alarm {} seconds from now.", initialDelay);
        return initialDelay;
    }
}
