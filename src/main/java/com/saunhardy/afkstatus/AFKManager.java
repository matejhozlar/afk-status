package com.saunhardy.afkstatus;

import com.saunhardy.afkstatus.storage.BlacklistStorage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.ChatFormatting;

import java.util.*;

public class AFKManager {
    private static final Set<String> blacklist = new HashSet<>();
    private static final Set<UUID> afkPlayers = new HashSet<>();
    private static final Map<UUID, Long> lastActivity = new HashMap<>();
    private static final Map<UUID, Long> afkStartTime = new HashMap<>();
    private static long getAfkTimeoutMs() {
        return Config.AFK_TRIGGER_TIMER.get() * 60L * 1000L;
    }

    private static ChatFormatting getConfiguredColor() {
        try {
            return ChatFormatting.valueOf(Config.MESSAGE_COLOR.get().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return ChatFormatting.YELLOW;
        }
    }

    public static void reloadBlacklist() {
        blacklist.clear();
        List<String> fileList = BlacklistStorage.loadBlacklist();
        fileList.forEach(entry -> blacklist.add(entry.toLowerCase()));
    }

    public static boolean isBlacklisted(ServerPlayer player) {
        String lowerName = player.getName().getString().toLowerCase();
        String uuidStr = player.getUUID().toString().toLowerCase();
        return blacklist.contains(lowerName) || blacklist.contains(uuidStr);
    }

    public static void setAFK(UUID uuid, boolean afk) {
        if (afk) {
            afkPlayers.add(uuid);
            afkStartTime.put(uuid, System.currentTimeMillis());
        } else {
            afkPlayers.remove(uuid);
            afkStartTime.remove(uuid);
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

            if (Config.SYSTEM_MESSAGES.get()) {
                String msg = player.getName().getString() + " is no longer AFK.";
                var server = player.getServer();
                if (server != null) {
                    server.getPlayerList().broadcastSystemMessage(
                            net.minecraft.network.chat.Component.literal(msg).withStyle(style -> style.withColor(getConfiguredColor())), false
                    );
                }
            }
        }
    }



    public static void checkAFKStatus(Collection<ServerPlayer> players) {
        long now = System.currentTimeMillis();
        List<ServerPlayer> toKick = new ArrayList<>();

        for (ServerPlayer player : players) {
            UUID uuid = player.getUUID();
            long last = lastActivity.getOrDefault(uuid, now);

            if (!isAFK(uuid) && now - last >= getAfkTimeoutMs()) {
                setAFK(uuid, true);
                AFKStatus.applyAFKTag(player, true);

                if (Config.SYSTEM_MESSAGES.get()) {
                    String msg = player.getName().getString() + " is now AFK.";
                    var server = player.getServer();
                    if (server != null) {
                        server.getPlayerList().broadcastSystemMessage(
                                net.minecraft.network.chat.Component.literal(msg).withStyle(style -> style.withColor(getConfiguredColor())), false
                        );
                    }
                }
            }

            int kickDelayMinutes = Config.AFK_KICK_TIMER.get();
            if (kickDelayMinutes > 0 && isAFK(uuid)) {
                if (isBlacklisted(player)) {
                    continue;
                }

                long afkTime = afkStartTime.getOrDefault(uuid, now);
                long kickDelayMs = kickDelayMinutes * 60L * 1000L;
                if (now - afkTime >= kickDelayMs) {
                    AFKStatus.applyAFKTag(player, false);
                    toKick.add(player);
                }
            }
        }

        for (ServerPlayer player : toKick) {
            String kickMsg = Config.KICK_MESSAGE.get();
            player.connection.disconnect(net.minecraft.network.chat.Component.literal(kickMsg));
        }
    }

    public static void removePlayerData(UUID uuid) {
        afkPlayers.remove(uuid);
        lastActivity.remove(uuid);
        afkStartTime.remove(uuid);
    }

}