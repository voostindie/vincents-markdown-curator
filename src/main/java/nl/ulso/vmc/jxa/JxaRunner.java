package nl.ulso.vmc.jxa;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

/**
 * Runs JXA scripts, returning the output as JSON objects.
 */
public interface JxaRunner
{
    /**
     * Runs a script and interprets its output as a JSON object.
     * @param name Name of the script to run without path and extension, e.g. "hello"
     * @param arguments Arguments to pass to the script.
     * @return The output of the script, parsed into a JSON object.
     */
    JsonObject runScriptForObject(String name, String... arguments);

    /**
     * Runs a script and interprets its output as a JSON array.
     * @param name Name of the script to run without path and extension, e.g. "hello"
     * @param arguments Arguments to pass to the script.
     * @return The output of the script, parsed into a JSON array.
     */
    JsonArray runScriptForArray(String name, String... arguments);
}
