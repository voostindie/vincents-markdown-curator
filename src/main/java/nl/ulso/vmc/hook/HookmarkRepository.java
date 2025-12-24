package nl.ulso.vmc.hook;

import jakarta.json.JsonValue;
import nl.ulso.jxa.JavaScriptForAutomation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;

/**
 * Fetches hooks (bookmarks) for a URI from <a href="https://hookproductivity.com">Hookmark</a>.
 * <p/>
 * This implementation uses JXA scripting.
 */
@Singleton
public class HookmarkRepository
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HookmarkRepository.class);
    private static final String SCRIPT = "hookmark-bookmarks";

    private final JavaScriptForAutomation jxa;

    @Inject
    public HookmarkRepository(JavaScriptForAutomation jxa)
    {
        this.jxa = jxa;
    }

    public List<Hook> listHooks(String documentUri)
    {
        LOGGER.debug("Loading hooks for URI: {}", documentUri);
        var array = jxa.runScriptForArray(SCRIPT, documentUri);
        return array.stream()
                .map(JsonValue::asJsonObject)
                .map(object -> new Hook(
                        object.getString("name"),
                        object.getString("address")))
                .toList();
    }
}
