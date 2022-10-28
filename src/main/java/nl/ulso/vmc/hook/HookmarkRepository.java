package nl.ulso.vmc.hook;

import nl.ulso.vmc.jxa.JxaRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.JsonValue;
import java.util.List;
import java.util.concurrent.*;

/**
 * Fetches hooks (bookmarks) for a URI from <a href="https://hookproductivity.com">Hookmark</a>.
 * <p/>
 * This implementation uses JXA scripting.
 */
@Singleton
public class HookmarkRepository
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HookmarkRepository.class);
    private static final long REFRESH_TIMEOUT = TimeUnit.MINUTES.toMillis(1);
    private static final String SCRIPT = "hookmark-bookmarks";

    private final ConcurrentMap<String, Cache> cacheMap;
    private final JxaRunner jxaRunner;

    @Inject
    public HookmarkRepository(JxaRunner jxaRunner)
    {
        this.cacheMap = new ConcurrentHashMap<>();
        this.jxaRunner = jxaRunner;
    }

    public List<Hook> listHooks(String documentUri)
    {
        var cache = cacheMap.get(documentUri);
        if (cache != null)
        {
            if (System.currentTimeMillis() - REFRESH_TIMEOUT < cache.lastUpdated())
            {
                LOGGER.debug("Getting hooks from cache for URI: {}", documentUri);
                return cache.hooks();
            }
        }
        LOGGER.debug("Loading hooks for URI: {}", documentUri);
        var array = jxaRunner.runScriptForArray(SCRIPT, documentUri);
        var hooks = array.stream()
                .map(JsonValue::asJsonObject)
                .map(object -> new Hook(
                        object.getString("name"),
                        object.getString("address")))
                .toList();
        cacheMap.put(documentUri, new Cache(System.currentTimeMillis(), hooks));
        return hooks;
    }

    record Cache(long lastUpdated, List<Hook> hooks)
    {
    }
}
