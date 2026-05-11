package com.saunhardy.afkstatus.neoforge;

import com.saunhardy.afkstatus.AFKStatus;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(AFKStatus.MOD_ID)
public final class AFKStatusNeoForge {

    public AFKStatusNeoForge(IEventBus modBus, ModContainer modContainer) {
        AFKStatus.init();
        NeoForge.EVENT_BUS.register(this);
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
    public void onServerTick(ServerTickEvent.Post event) {
        AFKStatus.onServerTick(event.getServer());
    }
}
