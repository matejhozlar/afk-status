package com.saunhardy.afkstatus;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<Integer> AFK_TRIGGER_TIMER = BUILDER
            .comment("Time in minutes before a player is considered AFK")
            .define("afkTriggerTimer", 5);

    public static final ModConfigSpec SPEC = BUILDER.build();
}