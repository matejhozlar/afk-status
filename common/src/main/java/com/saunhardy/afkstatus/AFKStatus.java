package com.saunhardy.afkstatus;

import com.mojang.brigadier.CommandDispatcher;
import com.saunhardy.afkstatus.command.AFKCommand;
import com.saunhardy.afkstatus.storage.BlacklistStorage;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AFKStatus {
    public static final String MOD_ID = "afkstatus";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private AFKStatus() {}

    private static volatile PlayerTeam afkTeam = null;
    private static final Map<UUID, BlockPos> lastPositions = new HashMap<>();
    private static final Map<UUID, Rotation> lastRotations = new HashMap<>();
    private static int tickCounter = 0;

    private record Rotation(float yaw, float pitch) {}

    public static void init() {
        Config.load();
        BlacklistStorage.ensureConfigFolder();
        AFKManager.reloadBlacklist();
    }

    public static void onPlayerLogin(ServerPlayer player) {
        UUID uuid = player.getUUID();
        lastPositions.remove(uuid);
        lastRotations.remove(uuid);
        AFKManager.setAFK(uuid, false);
        AFKManager.removePlayerData(uuid);
        applyAFKTag(player, false);
    }

    public static void onPlayerLogout(ServerPlayer player) {
        UUID uuid = player.getUUID();
        lastPositions.remove(uuid);
        lastRotations.remove(uuid);
        AFKManager.setAFK(uuid, false);
        AFKManager.removePlayerData(uuid);
        applyAFKTag(player, false);
    }

    public static void onChat(ServerPlayer player) {
        AFKManager.updateActivity(player.getUUID(), player);
    }

    public static void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        AFKCommand.register(dispatcher);
    }

    public static void onServerTick(MinecraftServer server) {
        if (server == null) return;

        tickCounter++;
        int interval = Config.checkIntervalTicks();
        if (tickCounter % interval != 0) return;
        tickCounter = 0;

        float rotEps = (float) Config.rotationThresholdDegrees();
        boolean rotationEnabled = Config.rotationEnabled();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID uuid = player.getUUID();
            BlockPos current = player.blockPosition();
            BlockPos previous = lastPositions.get(uuid);

            if (previous == null || !previous.equals(current)) {
                AFKManager.updateActivity(uuid, player);
                lastPositions.put(uuid, current);
            }

            if (rotationEnabled) {
                float yaw = player.getYRot();
                float pitch = player.getXRot();
                Rotation last = lastRotations.get(uuid);
                if (last == null
                        || Math.abs(yaw - last.yaw) >= rotEps
                        || Math.abs(pitch - last.pitch) >= rotEps) {
                    AFKManager.updateActivity(uuid, player);
                    lastRotations.put(uuid, new Rotation(yaw, pitch));
                }
            }
        }

        AFKManager.checkAFKStatus(server.getPlayerList().getPlayers());
    }

    public static void applyAFKTag(ServerPlayer player, boolean afk) {
        if (player.getServer() == null) return;
        Scoreboard scoreboard = player.getServer().getScoreboard();

        if (afkTeam == null) {
            synchronized (AFKStatus.class) {
                if (afkTeam == null) {
                    afkTeam = scoreboard.getPlayerTeam("afkstatus");
                    if (afkTeam == null) {
                        afkTeam = scoreboard.addPlayerTeam("afkstatus");
                        afkTeam.setColor(ChatFormatting.GRAY);
                        afkTeam.setPlayerPrefix(
                                Component.literal("[AFK] ").withStyle(s -> s.withColor(ChatFormatting.GRAY))
                        );
                    }
                }
            }
        }

        if (afk) {
            if (!afkTeam.getPlayers().contains(player.getScoreboardName())) {
                scoreboard.addPlayerToTeam(player.getScoreboardName(), afkTeam);
            }
        } else {
            if (afkTeam.getPlayers().contains(player.getScoreboardName())) {
                scoreboard.removePlayerFromTeam(player.getScoreboardName(), afkTeam);
            }
        }
    }
}
