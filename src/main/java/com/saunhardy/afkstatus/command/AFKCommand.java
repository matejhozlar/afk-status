package com.saunhardy.afkstatus.command;

import com.saunhardy.afkstatus.AFKManager;
import com.saunhardy.afkstatus.AFKStatus;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class AFKCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("afk")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayer();
                            boolean isAfk = AFKManager.isAFK(player.getUUID());

                            AFKManager.setAFK(player.getUUID(), !isAfk);
                            AFKStatus.applyAFKTag(player, !isAfk);

                            String name = player.getName().getString();
                            String msg = name + (isAfk ? " is no longer AFK." : " is now AFK.");

                            player.getServer().getPlayerList().broadcastSystemMessage(
                                    Component.literal(msg), false
                            );
                            return 1;
                        })
        );
    }
}
