package com.saunhardy.afkstatus.fabric;

import com.saunhardy.afkstatus.AFKStatus;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class AFKStatusFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        AFKStatus.init();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                AFKStatus.onPlayerLogin(handler.player));

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                AFKStatus.onPlayerLogout(handler.player));

        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) ->
                AFKStatus.onChat(sender));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                AFKStatus.onRegisterCommands(dispatcher));

        ServerTickEvents.END_SERVER_TICK.register(AFKStatus::onServerTick);
    }
}
