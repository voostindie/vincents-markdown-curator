package nl.ulso.vmc.omnifocus;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.json.JsonValue;
import nl.ulso.jxa.JavaScriptForAutomation;
import nl.ulso.markdown_curator.vault.VaultRefresher;
import org.slf4j.*;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.toMap;
import static nl.ulso.vmc.omnifocus.OmniFocusUpdate.OMNIFOCUS_CHANGE;
import static nl.ulso.vmc.omnifocus.Status.ACTIVE;
import static nl.ulso.vmc.omnifocus.Status.ON_HOLD;

/// Fetches projects from a folder in <a href="https://www.omnigroup.com/omnifocus">OmniFocus</a>.
///
/// This implementation uses JXA scripting. Data is refreshed every 5 minutes, independently of the
/// rest of the system, to ensure system overall throughput is not impacted. If the OmniFocus
/// database hasn't changed (based on the modification timestamp of the database folder), refresh is
/// skipped. If, after a refresh, a change is detected to the set of projects in memory, a change
/// object is published, requesting the system to process the update.
@Singleton
public final class OmniFocusRepository
{
    private static final Logger LOGGER = LoggerFactory.getLogger(OmniFocusRepository.class);

    private static final File DATABASE_PATH =
        Path.of(System.getProperty("user.home"), "Library", "Containers",
            "com.omnigroup.OmniFocus4", "Data", "Library", "Application Support",
            "OmniFocus", "OmniFocus.ofocus"
        ).toFile();
    private static final String JXA_SCRIPT = "omnifocus-projects";
    private static final ScheduledExecutorService REFRESH_EXECUTOR = newScheduledThreadPool(1);
    private static final int REFRESH_DELAY_MINUTES = 5;

    // Because fetching projects from OmniFocus is a scheduled activity, it might run in parallel
    // with access in the [OmniFocusAttributeProducer]. In practice this can't happen as the access
    // from the [OmniFocusAttributeProducer] always comes after the refresh; synchronization is
    // done through the [OmniFocusUpdate] change object: this registry creates that object. No
    // refresh, no object, no access. However, it's better to be safe than sorry.
    private final AtomicReference<Map<String, OmniFocusProject>> cache;
    private long lastModified = 0L;

    // The filtering on statuses is ideally done in the JXA script to limit the data pulled from
    // OmniFocus, but this broke in OmniFocus 4.3.3. Now the filtering is in here, in the client.
    private static final Set<Status> SELECTED_STATUSES = Set.of(ACTIVE, ON_HOLD);

    @Inject
    public OmniFocusRepository(
        VaultRefresher refresher, JavaScriptForAutomation jxa, OmniFocusSettings settings)
    {
        if (!DATABASE_PATH.canRead())
        {
            throw new IllegalStateException("OmniFocus database is inaccessible: " + DATABASE_PATH);
        }
        this.cache = new AtomicReference<>();
        scheduleBackgroundRefresh(refresher, jxa, settings);
    }

    private void scheduleBackgroundRefresh(
        VaultRefresher refresher, JavaScriptForAutomation jxa, OmniFocusSettings settings)
    {
        var curatorName = MDC.get("curator");
        REFRESH_EXECUTOR.scheduleAtFixedRate(() ->
            {
                MDC.put("curator", curatorName);
                if (lastModified == DATABASE_PATH.lastModified())
                {
                    LOGGER.debug("No changes in the OmniFocus database; skipping fetch.");
                    return;
                }
                var newProjects = fetchProjects(jxa, settings);
                var oldProjects = cache.getAndSet(newProjects);
                lastModified = DATABASE_PATH.lastModified();
                if (oldProjects == null)
                {
                    LOGGER.info("Initial fetch from OmniFocus completed.");
                    return;
                }
                if (newProjects.equals(oldProjects))
                {
                    LOGGER.debug("No changes in projects from OmniFocus; skipping refresh.");
                    return;
                }
                LOGGER.info("Relevant OmniFocus changes detected. Triggering a refresh.");
                refresher.triggerRefresh(OMNIFOCUS_CHANGE);
            }, 0, REFRESH_DELAY_MINUTES, MINUTES
        );
        LOGGER.info(
            "Scheduled background refresh of OmniFocus projects every {} minutes.",
            REFRESH_DELAY_MINUTES
        );
    }

    private Map<String, OmniFocusProject> fetchProjects(
        JavaScriptForAutomation jxa, OmniFocusSettings settings)
    {
        LOGGER.debug("Fetching OmniFocus projects in folder: {}", settings.omniFocusFolder());
        var array = jxa.runScriptForArray(JXA_SCRIPT, settings.omniFocusFolder());
        return array.stream()
            .map(JsonValue::asJsonObject)
            .map(object -> new OmniFocusProject(
                object.getString("id"),
                object.getString("name"),
                Status.fromString(object.getString("status")),
                object.getInt("priority")
            ))
            .filter(project -> SELECTED_STATUSES.contains(project.status()))
            .collect(toMap(OmniFocusProject::name, Function.identity()));
    }

    // If, at system start, the request for data comes before the data from OmniFocus
    // is available the system spins and waits.
    public void waitForInitialFetchToComplete()
    {
        Map<String, OmniFocusProject> result = null;
        while (result == null)
        {
            result = cache.get();
        }
        LOGGER.info("Fetched {} projects from OmniFocus.", result.size());
    }

    public Collection<OmniFocusProject> projects()
    {
        return cache.get().values();
    }
}