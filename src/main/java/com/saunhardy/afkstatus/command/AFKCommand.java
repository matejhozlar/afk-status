package com.saunhardy.afkstatus.command;

import com.saunhardy.afkstatus.AFKManager;
import com.saunhardy.afkstatus.AFKStatus;
import com.mojang.brigadier.CommandDispatcher;
import com.saunhardy.afkstatus.Config;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;
import java.util.UUID;

public class AFKCommand {
    private static ChatFormatting getConfiguredColor() {
        try {
            return ChatFormatting.valueOf(Config.MESSAGE_COLOR.get().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return ChatFormatting.YELLOW; // fallback
        }
    }


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

                                if (Config.SYSTEM_MESSAGES.get()) {
                                    String msg = player.getName().getString() + " is now AFK.";
                                    Objects.requireNonNull(player.getServer()).getPlayerList().broadcastSystemMessage(
                                            Component.literal(msg).withStyle(style -> style.withColor(getConfiguredColor())), false
                                    );
                                }
                            } else {
                                AFKManager.updateActivity(uuid, player);
                            }

                            return 1;
                        })
        );
    }
}