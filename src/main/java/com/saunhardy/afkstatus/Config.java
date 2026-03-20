package com.saunhardy.afkstatus;

import net.minecraftforge.common.ForgeConfigSpec;

public final class Config {
    private Config() {}

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    static {
        BUILDER.push("afk");

        AFK_TRIGGER_MINUTES = BUILDER
                .comment(
                        "How long a player must be inactive before being marked AFK.",
                        "Unit: minutes"
                )
                .defineInRange("triggerMinutes", 5, 1, 60);

        AFK_KICK_MINUTES = BUILDER
                .comment(
                        "How long after being marked AFK a player is kicked.",
                        "Set to 0 to disable kicking.",
                        "Unit: minutes"
                )
                .defineInRange("kickMinutes", 0, 0, 120);

        CHECK_INTERVAL_TICKS = BUILDER
                .comment(
                        "How often the server checks for AFK changes.",
                        "20 ticks = ~1 second",
                        "Lower = more responsive, higher = less overhead."
                )
                .defineInRange("checkIntervalTicks", 20, 1, 1200);

        BUILDER.pop();
    }

    static {
        BUILDER.push("detection");

        ROTATION_CHECK_ENABLED = BUILDER
                .comment(
                        "If true, mouse look (camera rotation) counts as activity.",
                        "Useful for builders who look around without moving."
                )
                .define("rotation.enabled", false);

        ROTATION_THRESHOLD_DEGREES = BUILDER
                .comment(
                        "Minimum change in yaw/pitch required to count as activity.",
                        "Ignored if rotation.enabled = false.",
                        "Unit: degrees"
                )
                .defineInRange("rotation.thresholdDegrees", 5, 1, 45);

        BUILDER.pop();
    }

    static {
        BUILDER.push("messages");

        SYSTEM_MESSAGES = BUILDER
                .comment(
                        "If true, broadcast messages like:",
                        "\"<name> is now AFK.\" and \"<name> is no longer AFK.\""
                )
                .define("systemMessages", true);

        MESSAGE_COLOR = BUILDER
                .comment(
                        "Color for AFK messages. Valid options: black, dark_blue, dark_green, dark_aqua, dark_red, dark_purple, gold, gray, dark_gray, blue, green, aqua, red, light_purple, yellow, white"
                )
                .define("messageColor", "yellow");

        KICK_MESSAGE = BUILDER
                .comment(
                        "Message shown to players when kicked for being AFK."
                )
                .define("kickMessage", "You were kicked for being AFK too long.");

        BUILDER.pop();
    }

    static {
        BUILDER.push("sleep");

        SLEEP_BYPASS_ENABLED = BUILDER
                .comment(
                        "If true, AFK players are excluded from the sleep vote.",
                        "This means non-AFK players can skip the night",
                        "without waiting for AFK players to get in bed."
                )
                .define("bypassEnabled", true);

        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static final ForgeConfigSpec.IntValue AFK_TRIGGER_MINUTES;
    public static final ForgeConfigSpec.IntValue AFK_KICK_MINUTES;
    public static final ForgeConfigSpec.IntValue CHECK_INTERVAL_TICKS;

    public static final ForgeConfigSpec.BooleanValue ROTATION_CHECK_ENABLED;
    public static final ForgeConfigSpec.IntValue ROTATION_THRESHOLD_DEGREES;

    public static final ForgeConfigSpec.BooleanValue SYSTEM_MESSAGES;
    public static final ForgeConfigSpec.ConfigValue<String> MESSAGE_COLOR;
    public static final ForgeConfigSpec.ConfigValue<String> KICK_MESSAGE;

    public static final ForgeConfigSpec.BooleanValue SLEEP_BYPASS_ENABLED;
}
