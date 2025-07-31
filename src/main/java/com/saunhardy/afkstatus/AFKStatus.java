package com.saunhardy.afkstatus;

import com.mojang.logging.LogUtils;
import com.saunhardy.afkstatus.command.AFKCommand;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Vec3i;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod(AFKStatus.MODID)
public class AFKStatus {
    public static final String MODID = "afkstatus";
    public static final Logger LOGGER = LogUtils.getLogger();

    private final Map<UUID, Vec3i> lastPositions = new HashMap<>();

    public AFKStatus(IEventBus modBus, ModContainer container) {
        NeoForge.EVENT_BUS.register(this);
        modBus.addListener(this::onInitialize);
    }

    private void onInitialize(FMLCommonSetupEvent ev) {
        // Setup if needed
    }

    @SubscribeEvent
    public void onRegister(RegisterCommandsEvent ev) {
        AFKCommand.register(ev.getDispatcher());
    }

    @SubscribeEvent
    public void onChat(ServerChatEvent ev) {
        AFKManager.updateActivity(ev.getPlayer().getUUID(), ev.getPlayer());
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post ev) {
        MinecraftServer server = ev.getServer();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID uuid = player.getUUID();
            Vec3i current = toVec3i(player.position());
            Vec3i previous = lastPositions.get(uuid);

            if (previous == null || !previous.equals(current)) {
                AFKManager.updateActivity(uuid, player); // 👈 updated
                lastPositions.put(uuid, current);
            }
        }

        AFKManager.checkAFKStatus(server.getPlayerList().getPlayers());
    }

    private Vec3i toVec3i(net.minecraft.world.phys.Vec3 pos) {
        return new Vec3i((int) pos.x, (int) pos.y, (int) pos.z);
    }

    public static void applyAFKTag(ServerPlayer player, boolean afk) {
        Scoreboard scoreboard = player.getServer().getScoreboard();
        String teamName = "afkstatus";

        PlayerTeam team = scoreboard.getPlayerTeam(teamName);
        if (team == null) {
            team = scoreboard.addPlayerTeam(teamName);
            team.setColor(ChatFormatting.GRAY);
            team.setPlayerPrefix(Component.literal("[AFK] ")
                    .withStyle(style -> style.withColor(ChatFormatting.GRAY)));
        }

        if (afk) {
            scoreboard.addPlayerToTeam(player.getScoreboardName(), team);
        } else {
            scoreboard.removePlayerFromTeam(player.getScoreboardName(), team);
        }
    }
}
