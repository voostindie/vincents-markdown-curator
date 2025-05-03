package nl.ulso.vmc.omnifocus;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.json.JsonValue;
import nl.ulso.markdown_curator.vault.VaultRefresher;
import nl.ulso.vmc.jxa.JxaRunner;
import org.slf4j.*;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.stream.Collectors.toMap;
import static nl.ulso.vmc.omnifocus.Status.ACTIVE;
import static nl.ulso.vmc.omnifocus.Status.ON_HOLD;
import static nl.ulso.vmc.omnifocus.Status.UNKNOWN;

/**
 * Fetches projects from a folder in <a href="https://www.omnigroup.com/omnifocus">OmniFocus</a>.
 * <p/>
 * This implementation uses JXA scripting. Data is refreshed every 5 minutes, independently of the
 * queries themselves, to ensure queries run efficiently. If the OmniFocus database hasn't changed
 * (based on the modification timestamp of the database folder), refresh is skipped. If, after a
 * refresh, a change is detected to the set of projects in memory, the vault is refreshed, forcing
 * it to re-run all queries and write changed documents.
 */
@Singleton
public class OmniFocusRepository
{
    private static final Logger LOGGER = LoggerFactory.getLogger(OmniFocusRepository.class);

    private static final File DATABASE_PATH =
            Path.of(System.getProperty("user.home"), "Library", "Containers",
                    "com.omnigroup.OmniFocus4", "Data", "Library", "Application Support",
                    "OmniFocus", "OmniFocus.ofocus").toFile();
    private static final String JXA_SCRIPT = "omnifocus-projects";
    private static final OmniFocusProject NULL_PROJECT =
            new OmniFocusProject("null", "null", UNKNOWN, -1);
    private static final ScheduledExecutorService REFRESH_EXECUTOR = newScheduledThreadPool(1);
    private static final int REFRESH_DELAY_MINUTES = 5;

    private final AtomicReference<Map<String, OmniFocusProject>> cache;
    private long lastModified = 0L;

    /**
     * The filtering on statuses is ideally done in the JXA script to limit the data pulled from
     * OmniFocus, but this broke in OmniFocus 4.3.3. Now the filtering is in here.
     */
    private static final Set<Status> SELECTED_STATUSES = Set.of(ACTIVE, ON_HOLD);

    @Inject
    public OmniFocusRepository(
            VaultRefresher refresher, JxaRunner jxaRunner, OmniFocusSettings settings)
    {
        if (!DATABASE_PATH.canRead())
        {
            throw new IllegalStateException("OmniFocus database is inaccessible: " + DATABASE_PATH);
        }
        this.cache = new AtomicReference<>();
        scheduleBackgroundRefresh(refresher, jxaRunner, settings);
    }

    private void scheduleBackgroundRefresh(
            VaultRefresher refresher, JxaRunner jxaRunner, OmniFocusSettings settings)
    {
        var curatorName = MDC.get("curator");
        REFRESH_EXECUTOR.scheduleAtFixedRate(() -> {
            MDC.put("curator", curatorName);
            if (lastModified == DATABASE_PATH.lastModified())
            {
                LOGGER.debug("No changes in the OmniFocus database; skipping fetch.");
                return;
            }
            var newProjects = fetchProjects(jxaRunner, settings);
            var oldProjects = cache.getAndSet(newProjects);
            lastModified = DATABASE_PATH.lastModified();
            if (oldProjects == null)
            {
                LOGGER.debug("Initial fetch from OmniFocus completed.");
                return;
            }
            if (newProjects.equals(oldProjects))
            {
                LOGGER.debug("No changes in projects fetched from OmniFocus; skipping refresh.");
                return;
            }
            LOGGER.info("Relevant OmniFocus changes detected. Triggering a refresh in the vault.");
            refresher.triggerRefresh();
        }, 0, REFRESH_DELAY_MINUTES, TimeUnit.MINUTES);
    }

    private Map<String, OmniFocusProject> fetchProjects(
            JxaRunner jxaRunner, OmniFocusSettings settings)
    {
        LOGGER.debug("Fetching OmniFocus projects in folder: {}", settings.omniFocusFolder());
        var array = jxaRunner.runScriptForArray(JXA_SCRIPT, settings.omniFocusFolder());
        return array.stream()
                .map(JsonValue::asJsonObject)
                .map(object -> new OmniFocusProject(
                        object.getString("id"),
                        object.getString("name"),
                        Status.fromString(object.getString("status")),
                        object.getInt("priority")))
                .filter(project -> SELECTED_STATUSES.contains(project.status()))
                .collect(toMap(OmniFocusProject::name, Function.identity()));
    }

    public Collection<OmniFocusProject> projects()
    {
        return cache.get().values();
    }

    public int priorityOf(String name)
    {
        return cache.get().getOrDefault(name, NULL_PROJECT).priority();
    }

    public Status statusOf(String name)
    {
        return cache.get().getOrDefault(name, NULL_PROJECT).status();
    }
}