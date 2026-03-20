package com.saunhardy.afkstatus;

import com.saunhardy.afkstatus.command.AFKCommand;
import com.saunhardy.afkstatus.storage.BlacklistStorage;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod(AFKStatus.MODID)
public class AFKStatus {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "afkstatus";

    private static volatile PlayerTeam afkTeam = null;
    private final Map<UUID, BlockPos> lastPositions = new HashMap<>();

    private final Map<UUID, Rotation> lastRotations = new HashMap<>();
    private static float rotThresholdDegrees() {
        return Config.ROTATION_THRESHOLD_DEGREES.get().floatValue();
    }

    private record Rotation(float yaw, float pitch){}

    private volatile MinecraftServer serverRef;

    public AFKStatus() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::onInitialize);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC, "afkstatus-server.toml");

        MinecraftForge.EVENT_BUS.register(this);

        BlacklistStorage.ensureConfigFolder();
        AFKManager.reloadBlacklist();
    }

    private void onInitialize(final FMLCommonSetupEvent ev) {

    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent evt) {
        this.serverRef = evt.getServer();
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent evt) {
        this.serverRef = null;
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            UUID uuid = player.getUUID();
            lastPositions.remove(uuid);
            lastRotations.remove(uuid);
            AFKManager.setAFK(uuid, false);
            AFKManager.removePlayerData(uuid);
            applyAFKTag(player, false);
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            UUID uuid = player.getUUID();
            lastPositions.remove(uuid);
            lastRotations.remove(uuid);
            AFKManager.setAFK(uuid, false);
            AFKManager.removePlayerData(uuid);
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
    public void onServerTick(TickEvent.ServerTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END) return;

        tickCounter++;
        int interval = Config.CHECK_INTERVAL_TICKS.get();
        if (tickCounter % interval != 0) return;
        tickCounter = 0;

        MinecraftServer server = this.serverRef;
        if (server == null) return;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID uuid = player.getUUID();
            BlockPos current = player.blockPosition();
            BlockPos previous = lastPositions.get(uuid);

            if (previous == null || !previous.equals(current)) {
                AFKManager.updateActivity(uuid, player);
                lastPositions.put(uuid, current);
            }

            if (Config.ROTATION_CHECK_ENABLED.get()) {
                float yaw = player.getYRot();
                float pitch = player.getXRot();
                Rotation last = lastRotations.get(uuid);
                float eps = rotThresholdDegrees();
                if (last == null
                        || Math.abs(yaw - last.yaw) >= eps
                        || Math.abs(pitch - last.pitch) >= eps) {
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
