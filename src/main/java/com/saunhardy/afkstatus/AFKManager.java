package com.saunhardy.afkstatus;

import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class AFKManager {
    private static final Set<UUID> afkPlayers = new HashSet<>();
    private static final Map<UUID, Long> lastActivity = new HashMap<>();
    private static long getAfkTimeoutMs() {
        return Config.AFK_TRIGGER_TIMER.get() * 60L * 1000L;
    }

    public static void setAFK(UUID uuid, boolean afk) {
        if (afk) {
            afkPlayers.add(uuid);
        } else {
            afkPlayers.remove(uuid);
            lastActivity.put(uuid, System.currentTimeMillis());
        }
    }

    public static boolean isAFK(UUID uuid) {
        return afkPlayers.contains(uuid);
    }

    public static void updateActivity(UUID uuid, ServerPlayer player) {
        lastActivity.put(uuid, System.currentTimeMillis());

        if (isAFK(uuid)) {
            setAFK(uuid, false);
            AFKStatus.applyAFKTag(player, false);

            String msg = player.getName().getString() + " is no longer AFK.";
            Objects.requireNonNull(player.getServer()).getPlayerList().broadcastSystemMessage(
                    net.minecraft.network.chat.Component.literal(msg), false
            );
        }
    }

    public static void checkAFKStatus(Collection<ServerPlayer> players) {
        long now = System.currentTimeMillis();
        for (ServerPlayer player : players) {
            UUID uuid = player.getUUID();
            long last = lastActivity.getOrDefault(uuid, now);
            if (!isAFK(uuid) && now - last >= getAfkTimeoutMs()) {
                setAFK(uuid, true);
                AFKStatus.applyAFKTag(player, true);

                String msg = player.getName().getString() + " is now AFK.";
                Objects.requireNonNull(player.getServer()).getPlayerList().broadcastSystemMessage(
                        net.minecraft.network.chat.Component.literal(msg), false
                );
            }
        }
    }
}