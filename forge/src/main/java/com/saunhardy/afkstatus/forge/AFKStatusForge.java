package com.saunhardy.afkstatus.forge;

import com.saunhardy.afkstatus.AFKStatus;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AFKStatus.MOD_ID)
public final class AFKStatusForge {

    private volatile MinecraftServer serverRef;

    public AFKStatusForge(FMLJavaModLoadingContext ignored) {
        AFKStatus.init();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        serverRef = event.getServer();
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        serverRef = null;
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            AFKStatus.onPlayerLogin(player);
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            AFKStatus.onPlayerLogout(player);
        }
    }

    @SubscribeEvent
    public void onChat(ServerChatEvent event) {
        AFKStatus.onChat(event.getPlayer());
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        AFKStatus.onRegisterCommands(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent.Post event) {
        MinecraftServer server = serverRef;
        if (server == null) return;
        AFKStatus.onServerTick(server);
    }
}
