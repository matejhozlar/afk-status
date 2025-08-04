package com.saunhardy.afkstatus.storage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.saunhardy.afkstatus.AFKStatus;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class BlacklistStorage {
    private static final Gson GSON = new Gson();
    private static final Path FOLDER = Paths.get("config", "AFKStatus");
    private static final Path FILE = FOLDER.resolve("afk_blacklist.json");

    /**
     * Ensure config folder exists.
     */
    public static void ensureConfigFolder() {
        try {
            Files.createDirectories(FOLDER);
        } catch (IOException e) {
            AFKStatus.LOGGER.error("Failed to create AFKStatus config folder", e);
        }
    }

    /**
     * Loads the blacklist from JSON file.
     * Returns empty list if file doesn't exist or on error.
     */
    public static List<String> loadBlacklist() {
        try {
            if (Files.exists(FILE)) {
                String json = Files.readString(FILE, StandardCharsets.UTF_8);
                Type listType = new TypeToken<List<String>>(){}.getType();
                List<String> list = GSON.fromJson(json, listType);
                return list != null ? list : Collections.emptyList();
            }
        } catch (IOException e) {
            AFKStatus.LOGGER.error("Failed to load AFK blacklist from file", e);
        } catch (com.google.gson.JsonSyntaxException e) {
            AFKStatus.LOGGER.error("Malformed AFK blacklist JSON", e);
        }
        return Collections.emptyList();
    }

    /**
     * Saves the blacklist to JSON file.
     */
    public static void saveBlacklist(List<String> blacklist) {
        try {
            String json = GSON.toJson(blacklist);
            // Folder creation assumed done already in ensureConfigFolder()
            Files.writeString(FILE, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            AFKStatus.LOGGER.error("Failed to save AFK blacklist to file", e);
        }
    }
}
