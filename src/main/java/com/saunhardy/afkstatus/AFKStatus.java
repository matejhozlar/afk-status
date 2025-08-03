package com.saunhardy.afkstatus;

import com.saunhardy.afkstatus.command.AFKCommand;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.core.BlockPos;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.minecraft.ChatFormatting;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Mod(AFKStatus.MODID)
public class AFKStatus {
    public static final String MODID = "afkstatus";
    private static PlayerTeam afkTeam = null;

    private final Map<UUID, BlockPos> lastPositions = new HashMap<>();

    public AFKStatus(IEventBus modBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(this);
        modBus.addListener(this::onInitialize);
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }

    private void onInitialize(FMLCommonSetupEvent ev) {
        // Setup if needed
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            UUID uuid = player.getUUID();
            lastPositions.remove(uuid);
            AFKManager.setAFK(uuid, false);
            applyAFKTag(player, false);
        }
    }

    @SubscribeEvent
    public void onRegister(RegisterCommandsEvent ev) {
        AFKCommand.register(ev.getDispatcher());
    }

    @SubscribeEvent
    public void onChat(ServerChatEvent ev) {
        AFKManager.updateActivity(ev.getPlayer().getUUID(), ev.getPlayer());
    }

    private int tickCounter = 0;

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post ev) {
        tickCounter++;
        int interval = Config.CHECK_INTERVAL_TICKS.get();

        if (tickCounter % interval != 0) return;

        tickCounter = 0;

        MinecraftServer server = ev.getServer();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID uuid = player.getUUID();
            BlockPos current = player.blockPosition();
            BlockPos previous = lastPositions.get(uuid);

            if (previous == null || !previous.equals(current)) {
                AFKManager.updateActivity(uuid, player);
                lastPositions.put(uuid, current);
            }
        }

        AFKManager.checkAFKStatus(server.getPlayerList().getPlayers());
    }

    public static void applyAFKTag(ServerPlayer player, boolean afk) {
        Scoreboard scoreboard = Objects.requireNonNull(player.getServer()).getScoreboard();

        if (afkTeam == null) {
            afkTeam = scoreboard.getPlayerTeam("afkstatus");
            if (afkTeam == null) {
                afkTeam = scoreboard.addPlayerTeam("afkstatus");
                afkTeam.setColor(ChatFormatting.GRAY);
                afkTeam.setPlayerPrefix(Component.literal("[AFK] ").withStyle(s -> s.withColor(ChatFormatting.GRAY)));
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