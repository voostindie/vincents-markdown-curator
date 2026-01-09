package nl.ulso.vmc.obsidian;

import jakarta.json.Json;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParsingException;
import nl.ulso.markdown_curator.Changelog;
import nl.ulso.markdown_curator.query.*;
import nl.ulso.markdown_curator.vault.FileSystemVault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.*;
import java.nio.file.Path;
import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

@Singleton
public class StarredDocumentsQuery
        implements Query
{
    private static final Logger LOGGER = LoggerFactory.getLogger(StarredDocumentsQuery.class);
    private static final Path STARRED_FILE = Path.of(".obsidian", "starred.json");

    private final FileSystemVault vault;
    private final QueryResultFactory resultFactory;
    private Cache cache;

    @Inject
    StarredDocumentsQuery(FileSystemVault vault, QueryResultFactory resultFactory)
    {
        this.vault = vault;
        this.resultFactory = resultFactory;
        this.cache = new Cache(-1, emptyList());
    }

    @Override
    public String name()
    {
        return "starred";
    }

    @Override
    public String description()
    {
        return "Lists all titles starred in Obsidian";
    }

    @Override
    public Map<String, String> supportedConfiguration()
    {
        return emptyMap();
    }

    @Override
    public boolean isImpactedBy(Changelog changelog, QueryDefinition definition)
    {
        return true;
    }

    @Override
    public synchronized QueryResult run(QueryDefinition definition)
    {
        var file = vault.root().resolve(STARRED_FILE).toFile();
        if (!file.exists())
        {
            return resultFactory.error(
                    "Configuration file `" + STARRED_FILE + "` not found.\n\n" +
                    "Is this an Obsidian vault?");
        }
        var lastModified = file.lastModified();
        if (cache.lastModified != lastModified)
        {
            try (var inputStream = new BufferedInputStream(new FileInputStream(file)))
            {
                cache = new Cache(lastModified, extractTitles(inputStream));
            }
            catch (IOException | JsonParsingException e)
            {
                LOGGER.error("Couldn't parse configuration file '{}'.", e.getMessage(), e);
                return resultFactory.error(
                        "Configuration file `" + STARRED_FILE +
                        "` couldn't be parsed.\n\nTechnical error: " + e.getMessage());
            }
        }
        return resultFactory.unorderedList(cache.titles);
    }

    private static List<String> extractTitles(BufferedInputStream inputStream)
    {
        return Json.createReader(inputStream)
                .readObject()
                .getJsonArray("items")
                .stream()
                .map(StarredDocumentsQuery::readTitle)
                .filter(Objects::nonNull)
                .map(title -> "[[" + title + "]]")
                .toList();
    }

    private static String readTitle(JsonValue value)
    {
        var object = value.asJsonObject();
        if ("file".contentEquals(object.getString("type")))
        {
            return object.getString("title");
        }
        return null;
    }

    private record Cache(long lastModified, List<String> titles) {}
}
