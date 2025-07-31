package com.saunhardy.afkstatus.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class AFKCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("afk")
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            String playerName = source.getDisplayName().getString();
                            source.getServer().getPlayerList().broadcastSystemMessage(
                                    Component.literal(playerName + " is now AFK."),
                                    false
                            );
                            return 1;
                        })
        );
    }
}