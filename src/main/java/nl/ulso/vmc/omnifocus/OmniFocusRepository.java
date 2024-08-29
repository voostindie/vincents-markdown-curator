package nl.ulso.vmc.omnifocus;

import jakarta.json.JsonValue;
import nl.ulso.vmc.jxa.JxaRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.function.Function;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static nl.ulso.vmc.omnifocus.Status.ACTIVE;
import static nl.ulso.vmc.omnifocus.Status.ON_HOLD;
import static nl.ulso.vmc.omnifocus.Status.UNKNOWN;

/**
 * Fetches projects from a folder in <a href="https://www.omnigroup.com/omnifocus">OmniFocus</a>.
 * <p/>
 * This implementation uses JXA scripting. Data is refreshed at most once every minute to ensure
 * queries run efficiently.
 */

@Singleton
public class OmniFocusRepository
{
    private static final Logger LOGGER = LoggerFactory.getLogger(OmniFocusRepository.class);
    private static final String SCRIPT = "omnifocus-projects";
    private static final int REFRESH_INTERVAL = 60000;

    private Map<String, OmniFocusProject> projects;
    private long lastUpdated;
    private final JxaRunner jxaRunner;
    private final OmniFocusSettings settings;
    private static final OmniFocusProject NULL_PROJECT =
            new OmniFocusProject("null", "null", UNKNOWN, -1);
    /**
     * The filtering on statuses is ideally done in the JXA script to limit the data pulled from
     * OmniFocus, but this broke in OmniFocus 4.3.3. Now the filtering is in here.
     */
    private static final Set<Status> SELECTED_STATUSES = Set.of(ACTIVE, ON_HOLD);

    @Inject
    public OmniFocusRepository(JxaRunner jxaRunner, OmniFocusSettings settings)
    {
        this.jxaRunner = jxaRunner;
        this.settings = settings;
        projects = emptyMap();
        lastUpdated = -1L;
    }

    public Collection<OmniFocusProject> projects()
    {
        refresh();
        return projects.values();
    }

    public int priorityOf(String name)
    {
        refresh();
        return projects.getOrDefault(name, NULL_PROJECT).priority();
    }

    public Status statusOf(String name)
    {
        refresh();
        return projects.getOrDefault(name, NULL_PROJECT).status();
    }

    private void refresh()
    {
        // Bail out as quickly as possible: if the last refresh was recent
        if (lastUpdated > 0 && REFRESH_INTERVAL > System.currentTimeMillis() - lastUpdated)
        {
            return;
        }
        // Make sure at most one refresh job is running concurrently
        synchronized (this)
        {
            // Bail out as quickly as possible: if another refresh job just finished
            if (lastUpdated > 0 && REFRESH_INTERVAL > System.currentTimeMillis() - lastUpdated)
            {
                return;
            }
            // Now we have no choice but to refresh.
            LOGGER.debug("Refreshing OmniFocus projects in folder: {}", settings.omniFocusFolder());
            var array = jxaRunner.runScriptForArray(SCRIPT, settings.omniFocusFolder());
            projects = array.stream()
                    .map(JsonValue::asJsonObject)
                    .map(object -> new OmniFocusProject(
                            object.getString("id"),
                            object.getString("name"),
                            Status.fromString(object.getString("status")),
                            object.getInt("priority")))
                    .filter(project -> SELECTED_STATUSES.contains(project.status()))
                    .collect(toMap(OmniFocusProject::name, Function.identity()));
            lastUpdated = System.currentTimeMillis();
        }
    }
}