package com.saunhardy.afkstatus;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<Integer> AFK_TRIGGER_TIMER = BUILDER
            .comment("Time in minutes before a player is considered AFK")
            .defineInRange("afkTriggerTimer", 5, 1, 60);

    public static final ModConfigSpec.ConfigValue<Boolean> SYSTEM_MESSAGES = BUILDER
            .comment("Disable system messages (Player is now AFK., ...)")
            .define("systemMessages", true);

    public static final ModConfigSpec SPEC = BUILDER.build();
}