package nl.ulso.vmc.jxa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.json.*;
import javax.json.stream.JsonParsingException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

import static java.util.stream.Collectors.joining;

/**
 * Runs JXA scripts located on the classpath.
 * <p/>
 * Scripts are resolved from the classpath in the "/jxa" folder. To load and execute scripts
 * efficiently, the sources are first compiled. The compiled script is then reused.
 * <p/>
 * Because queries are executed in parallel, the same script might be compiled a few times; the
 * code in this class doesn't protect against that. It's a bit of a waste, but not worth the
 * complexity in code. In the end the same script will be executed over and over again.
 */
@Singleton
public class JxaClasspathRunner
        implements JxaRunner
{
    private static final Logger LOGGER = LoggerFactory.getLogger(JxaClasspathRunner.class);
    private static final String SOURCE_PACKAGE = "/jxa/";
    private static final String SOURCE_EXTENSION = ".js";
    private static final String COMPILED_SCRIPT_EXTENSION = ".scpt";
    private static final String COMPILE = "/usr/bin/osacompile";
    private static final String EXECUTE = "/usr/bin/osascript";
    private static final int MAX_COMPILATION_TIME_SECONDS = 5;

    private final ConcurrentMap<String, Path> scriptCache = new ConcurrentHashMap<>();

    @Override
    public JsonObject runScriptForObject(String name, String... arguments)
    {
        return runScript(name, arguments, JsonReader::readObject);
    }

    @Override
    public JsonArray runScriptForArray(String name, String... arguments)
    {
        return runScript(name, arguments, JsonReader::readArray);
    }

    private <J extends JsonStructure> J runScript(
            String name, String[] arguments, Function<JsonReader, J> jsonProcessor)
    {
        var path = resolveCompiledScript(name);
        var command = new ArrayList<>(List.of(EXECUTE, path.toString()));
        if (arguments != null)
        {
            command.addAll(Arrays.asList(arguments));
        }
        try
        {
            LOGGER.debug("Running compiled script '{}' from path {}", name, path);
            var process = new ProcessBuilder(command).start();
            try (var reader = process.inputReader())
            {
                var output = reader.lines().collect(joining(System.lineSeparator()));
                var jsonReader = Json.createReader(new StringReader(output));
                return jsonProcessor.apply(jsonReader);
            }
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Couldn't run compiled script: " + name, e);
        }
        catch (JsonParsingException e)
        {
            throw new IllegalStateException("Couldn't parse script output: " + name, e);
        }
    }

    private Path resolveCompiledScript(String scriptName)
    {
        var scriptPath = scriptCache.get(scriptName);
        if (scriptPath != null && Files.exists(scriptPath))
        {
            LOGGER.trace("Reusing compiled script '{}' from path {}", scriptName, scriptPath);
            return scriptPath;
        }
        scriptCache.remove(scriptName);
        return scriptCache.computeIfAbsent(scriptName,
                name -> compileScript(name, loadScriptSource(scriptName)));
    }

    private List<String> loadScriptSource(String scriptName)
    {
        String sourcePath = SOURCE_PACKAGE + scriptName + SOURCE_EXTENSION;
        LOGGER.debug("Loading script '{}' from classpath: {}", scriptName, sourcePath);
        try (var inputStream = this.getClass().getResourceAsStream(sourcePath))
        {
            if (inputStream == null)
            {
                throw new IllegalStateException("Couldn't locate script: " + scriptName);
            }
            try (var reader = new BufferedReader(new InputStreamReader(inputStream)))
            {
                return reader.lines().toList();
            }
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Couldn't load script: " + scriptName, e);
        }
    }

    private Path compileScript(String scriptName, List<String> source)
    {
        try
        {
            var outputFile = File.createTempFile(scriptName + "-", COMPILED_SCRIPT_EXTENSION);
            var outputPath = outputFile.toPath();
            LOGGER.debug("Compiling script '{}' to path: {}", scriptName, outputPath);
            var process = new ProcessBuilder(
                    COMPILE,
                    "-l", "JavaScript",
                    "-o", outputPath.toString(),
                    "-").start();
            try (var writer = process.outputWriter())
            {
                for (String line : source)
                {
                    writer.write(line);
                    writer.newLine();
                }
                writer.flush();
            }
            process.waitFor(MAX_COMPILATION_TIME_SECONDS, TimeUnit.SECONDS);
            return outputPath;
        }
        catch (IOException | InterruptedException e)
        {
            throw new IllegalStateException("Couldn't compile script: " + scriptName, e);
        }
    }
}
