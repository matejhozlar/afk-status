package com.saunhardy.afkstatus;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.IntValue AFK_TRIGGER_TIMER = BUILDER
            .comment("Time in minutes before a player is considered AFK")
            .defineInRange("afkTriggerTimer", 5, 1, 60);

    public static final ForgeConfigSpec.IntValue AFK_KICK_TIMER = BUILDER
            .comment("Time in minutes after a player is marked AFK before they are kicked (0 to disable)")
            .defineInRange("afkKickTimer", 0, 0, 120);

    public static final ForgeConfigSpec.ConfigValue<String> KICK_MESSAGE = BUILDER
            .comment("Kick message shown to players kicked for being AFK")
            .define("kickMessage", "You were kicked for being AFK too long.");

    public static final ForgeConfigSpec.BooleanValue SYSTEM_MESSAGES = BUILDER
            .comment("System messages (Player is now AFK., ...)")
            .define("systemMessages", true);

    public static final ForgeConfigSpec.IntValue CHECK_INTERVAL_TICKS = BUILDER
            .comment("How often (in ticks) to check for AFK players (20 ticks = 1 second)")
            .defineInRange("checkIntervalTicks", 20, 1, 1200);

    public static final ForgeConfigSpec.ConfigValue<String> MESSAGE_COLOR = BUILDER
            .comment("Color for AFK messages. Valid options: black, dark_blue, dark_green, dark_aqua, dark_red, dark_purple, gold, gray, dark_gray, blue, green, aqua, red, light_purple, yellow, white")
            .define("messageColor", "yellow");

    public static final ForgeConfigSpec SPEC = BUILDER.build();
}
