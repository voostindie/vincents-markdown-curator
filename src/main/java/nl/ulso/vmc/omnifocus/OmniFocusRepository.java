package nl.ulso.vmc.omnifocus;

import jakarta.json.JsonValue;
import nl.ulso.vmc.jxa.JxaRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Collections.emptyList;

/**
 * Fetches projects from a folder in <a href="https://www.omnigroup.com/omnifocus">OmniFocus</a>.
 * <p/>
 * This implementation uses JXA scripting.
 */

@Singleton
public class OmniFocusRepository
{
    private static final Logger LOGGER = LoggerFactory.getLogger(OmniFocusRepository.class);
    private static final String SCRIPT = "omnifocus-projects";

    private final AtomicReference<List<OmniFocusProject>> projects;
    private final ConcurrentMap<String, Long> lastUpdated;
    private final JxaRunner jxaRunner;

    @Inject
    public OmniFocusRepository(JxaRunner jxaRunner)
    {
        this.jxaRunner = jxaRunner;
        projects = new AtomicReference<>(emptyList());
        lastUpdated = new ConcurrentHashMap<>();
    }

    public List<OmniFocusProject> projects(String folder, int refreshInterval)
    {
        refresh(folder, refreshInterval);
        return projects.get();
    }

    private void refresh(String folder, long interval)
    {
        long lastUpdate = lastUpdated.getOrDefault(folder, 0L);
        if (System.currentTimeMillis() - interval < lastUpdate)
        {
            return;
        }
        LOGGER.debug("Refreshing OmniFocus projects in folder: {}", folder);
        var array = jxaRunner.runScriptForArray(SCRIPT, folder);
        projects.set(array.stream()
                .map(JsonValue::asJsonObject)
                .map(object ->
                        new OmniFocusProject(
                                object.getString("id"),
                                object.getString("name")))
                .toList());
        lastUpdated.put(folder, System.currentTimeMillis());
    }
}