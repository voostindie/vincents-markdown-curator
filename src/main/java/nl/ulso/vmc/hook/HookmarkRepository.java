package nl.ulso.vmc.hook;

import jakarta.json.JsonValue;
import nl.ulso.vmc.jxa.JxaRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
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

    private final JxaRunner jxaRunner;

    @Inject
    public HookmarkRepository(JxaRunner jxaRunner)
    {
        this.jxaRunner = jxaRunner;
    }

    public List<Hook> listHooks(String documentUri)
    {
        LOGGER.debug("Loading hooks for URI: {}", documentUri);
        var array = jxaRunner.runScriptForArray(SCRIPT, documentUri);
        return array.stream()
                .map(JsonValue::asJsonObject)
                .map(object -> new Hook(
                        object.getString("name"),
                        object.getString("address")))
                .toList();
    }
}
