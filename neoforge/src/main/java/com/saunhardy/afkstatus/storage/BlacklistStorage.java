package com.saunhardy.afkstatus.storage;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.saunhardy.afkstatus.AFKStatus;
import com.saunhardy.afkstatus.platform.Services;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public final class BlacklistStorage {
    private BlacklistStorage() {}

    private static final Gson GSON = new Gson();

    private static Path folder() {
        return Services.INSTANCE.getConfigDir().resolve("AFKStatus");
    }

    private static Path file() {
        return folder().resolve("afk_blacklist.json");
    }

    public static void ensureConfigFolder() {
        try {
            Files.createDirectories(folder());
        } catch (IOException e) {
            AFKStatus.LOGGER.error("Failed to create AFKStatus config folder", e);
        }
    }

    public static List<String> loadBlacklist() {
        Path file = file();
        try {
            if (Files.exists(file)) {
                String json = Files.readString(file, StandardCharsets.UTF_8);
                Type listType = new TypeToken<List<String>>() {}.getType();
                List<String> list = GSON.fromJson(json, listType);
                return list != null ? list : Collections.emptyList();
            }
        } catch (IOException e) {
            AFKStatus.LOGGER.error("Failed to load AFK blacklist from file", e);
        } catch (JsonSyntaxException e) {
            AFKStatus.LOGGER.error("Malformed AFK blacklist JSON", e);
        }
        return Collections.emptyList();
    }

    public static void saveBlacklist(List<String> blacklist) {
        try {
            Files.createDirectories(folder());
            String json = GSON.toJson(blacklist);
            Files.writeString(file(), json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            AFKStatus.LOGGER.error("Failed to save AFK blacklist to file", e);
        }
    }
}
