package com.saunhardy.afkstatus;

import com.saunhardy.afkstatus.platform.Services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class Config {
    private Config() {}

    private static final String FILE_NAME = "afkstatus.properties";
    private static final String DEFAULTS = """
            # AFKStatus configuration

            # How long a player must be inactive before being marked AFK.
            # Unit: minutes (1-60)
            afk.triggerMinutes=5

            # How long after being marked AFK a player is kicked.
            # Set to 0 to disable kicking.
            # Unit: minutes (0-120)
            afk.kickMinutes=0

            # How often the server checks for AFK changes.
            # 20 ticks = ~1 second. Lower = more responsive, higher = less overhead.
            # Range: 1-1200
            afk.checkIntervalTicks=20

            # If true, mouse look (camera rotation) counts as activity.
            # Useful for builders who look around without moving.
            detection.rotation.enabled=false

            # Minimum change in yaw/pitch required to count as activity.
            # Ignored if detection.rotation.enabled = false.
            # Unit: degrees (1-45)
            detection.rotation.thresholdDegrees=5

            # If true, broadcast messages like "<name> is now AFK."
            # and "<name> is no longer AFK."
            messages.systemMessages=true

            # Color for AFK messages.
            # Valid: black, dark_blue, dark_green, dark_aqua, dark_red, dark_purple,
            # gold, gray, dark_gray, blue, green, aqua, red, light_purple, yellow, white
            messages.messageColor=yellow

            # Message shown to players when kicked for being AFK.
            messages.kickMessage=You were kicked for being AFK too long.

            # If true, AFK players are excluded from the sleep vote.
            # Non-AFK players can skip the night without waiting for AFK players.
            sleep.bypassEnabled=true
            """;

    private static int afkTriggerMinutes = 5;
    private static int afkKickMinutes = 0;
    private static int checkIntervalTicks = 20;
    private static boolean rotationEnabled = false;
    private static int rotationThresholdDegrees = 5;
    private static boolean systemMessages = true;
    private static String messageColor = "yellow";
    private static String kickMessage = "You were kicked for being AFK too long.";
    private static boolean sleepBypassEnabled = true;

    public static int afkTriggerMinutes() { return afkTriggerMinutes; }
    public static int afkKickMinutes() { return afkKickMinutes; }
    public static int checkIntervalTicks() { return checkIntervalTicks; }
    public static boolean rotationEnabled() { return rotationEnabled; }
    public static int rotationThresholdDegrees() { return rotationThresholdDegrees; }
    public static boolean systemMessages() { return systemMessages; }
    public static String messageColor() { return messageColor; }
    public static String kickMessage() { return kickMessage; }
    public static boolean sleepBypassEnabled() { return sleepBypassEnabled; }

    public static void load() {
        Path file = Services.INSTANCE.getConfigDir().resolve(FILE_NAME);
        try {
            if (!Files.exists(file)) {
                Files.createDirectories(file.getParent());
                Files.writeString(file, DEFAULTS, StandardCharsets.UTF_8);
                AFKStatus.LOGGER.info("Wrote default AFKStatus config to {}", file);
            }

            Properties props = new Properties();
            try (var in = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                props.load(in);
            }

            afkTriggerMinutes = clamp(parseInt(props, "afk.triggerMinutes", afkTriggerMinutes), 1, 60);
            afkKickMinutes = clamp(parseInt(props, "afk.kickMinutes", afkKickMinutes), 0, 120);
            checkIntervalTicks = clamp(parseInt(props, "afk.checkIntervalTicks", checkIntervalTicks), 1, 1200);
            rotationEnabled = parseBool(props, "detection.rotation.enabled", rotationEnabled);
            rotationThresholdDegrees = clamp(parseInt(props, "detection.rotation.thresholdDegrees", rotationThresholdDegrees), 1, 45);
            systemMessages = parseBool(props, "messages.systemMessages", systemMessages);
            messageColor = props.getProperty("messages.messageColor", messageColor);
            kickMessage = props.getProperty("messages.kickMessage", kickMessage);
            sleepBypassEnabled = parseBool(props, "sleep.bypassEnabled", sleepBypassEnabled);
        } catch (IOException e) {
            AFKStatus.LOGGER.error("Failed to load AFKStatus config, using defaults", e);
        }
    }

    private static int parseInt(Properties props, String key, int fallback) {
        String raw = props.getProperty(key);
        if (raw == null) return fallback;
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            AFKStatus.LOGGER.warn("Invalid integer for {} = '{}', using {}", key, raw, fallback);
            return fallback;
        }
    }

    private static boolean parseBool(Properties props, String key, boolean fallback) {
        String raw = props.getProperty(key);
        if (raw == null) return fallback;
        return Boolean.parseBoolean(raw.trim());
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
