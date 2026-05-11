package com.saunhardy.afkstatus;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.saunhardy.afkstatus.platform.Services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Config {
    private Config() {}

    private static final String FILE_NAME = "afkstatus-server.toml";

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
            Files.createDirectories(file.getParent());
        } catch (IOException e) {
            AFKStatus.LOGGER.error("Failed to create AFKStatus config directory", e);
            return;
        }

        CommentedFileConfig config = CommentedFileConfig.builder(file)
                .autosave()
                .preserveInsertionOrder()
                .sync()
                .build();
        try {
            config.load();

            afkTriggerMinutes = clamp(config.getIntOrElse("afk.triggerMinutes", afkTriggerMinutes), 1, 60);
            config.set("afk.triggerMinutes", afkTriggerMinutes);
            config.setComment("afk.triggerMinutes",
                    " How long a player must be inactive before being marked AFK.\n Unit: minutes (1-60)");

            afkKickMinutes = clamp(config.getIntOrElse("afk.kickMinutes", afkKickMinutes), 0, 120);
            config.set("afk.kickMinutes", afkKickMinutes);
            config.setComment("afk.kickMinutes",
                    " How long after being marked AFK a player is kicked.\n Set to 0 to disable kicking.\n Unit: minutes (0-120)");

            checkIntervalTicks = clamp(config.getIntOrElse("afk.checkIntervalTicks", checkIntervalTicks), 1, 1200);
            config.set("afk.checkIntervalTicks", checkIntervalTicks);
            config.setComment("afk.checkIntervalTicks",
                    " How often the server checks for AFK changes.\n 20 ticks = ~1 second. Lower = more responsive, higher = less overhead.\n Range: 1-1200");

            rotationEnabled = config.getOrElse("detection.rotation.enabled", rotationEnabled);
            config.set("detection.rotation.enabled", rotationEnabled);
            config.setComment("detection.rotation.enabled",
                    " If true, mouse look (camera rotation) counts as activity.\n Useful for builders who look around without moving.");

            rotationThresholdDegrees = clamp(config.getIntOrElse("detection.rotation.thresholdDegrees", rotationThresholdDegrees), 1, 45);
            config.set("detection.rotation.thresholdDegrees", rotationThresholdDegrees);
            config.setComment("detection.rotation.thresholdDegrees",
                    " Minimum change in yaw/pitch required to count as activity.\n Ignored if detection.rotation.enabled = false.\n Unit: degrees (1-45)");

            systemMessages = config.getOrElse("messages.systemMessages", systemMessages);
            config.set("messages.systemMessages", systemMessages);
            config.setComment("messages.systemMessages",
                    " If true, broadcast messages like \"<name> is now AFK.\"\n and \"<name> is no longer AFK.\"");

            messageColor = config.getOrElse("messages.messageColor", messageColor);
            config.set("messages.messageColor", messageColor);
            config.setComment("messages.messageColor",
                    " Color for AFK messages.\n Valid: black, dark_blue, dark_green, dark_aqua, dark_red, dark_purple,\n gold, gray, dark_gray, blue, green, aqua, red, light_purple, yellow, white");

            kickMessage = config.getOrElse("messages.kickMessage", kickMessage);
            config.set("messages.kickMessage", kickMessage);
            config.setComment("messages.kickMessage",
                    " Message shown to players when kicked for being AFK.");

            sleepBypassEnabled = config.getOrElse("sleep.bypassEnabled", sleepBypassEnabled);
            config.set("sleep.bypassEnabled", sleepBypassEnabled);
            config.setComment("sleep.bypassEnabled",
                    " If true, AFK players are excluded from the sleep vote.\n Non-AFK players can skip the night without waiting for AFK players.");

            config.save();
        } catch (Exception e) {
            AFKStatus.LOGGER.error("Failed to load AFKStatus config; using defaults", e);
        } finally {
            config.close();
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
