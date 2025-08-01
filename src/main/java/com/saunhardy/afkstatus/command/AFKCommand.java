package com.saunhardy.afkstatus.command;

import com.saunhardy.afkstatus.AFKManager;
import com.saunhardy.afkstatus.AFKStatus;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;
import java.util.UUID;

public class AFKCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("afk")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayer();
                            assert player != null;
                            UUID uuid = player.getUUID();

                            boolean currentlyAFK = AFKManager.isAFK(uuid);
                            boolean newAFK = !currentlyAFK;

                            if (newAFK) {
                                AFKManager.setAFK(uuid, true);
                                AFKStatus.applyAFKTag(player, true);

                                String msg = player.getName().getString() + " is now AFK.";
                                Objects.requireNonNull(player.getServer()).getPlayerList().broadcastSystemMessage(
                                        Component.literal(msg), false
                                );
                            } else {
                                AFKManager.updateActivity(uuid, player);
                            }

                            return 1;
                        })
        );
    }
}


