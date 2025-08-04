package com.saunhardy.afkstatus.command;

import com.saunhardy.afkstatus.storage.BlacklistStorage;
import com.saunhardy.afkstatus.AFKManager;
import com.saunhardy.afkstatus.AFKStatus;
import com.saunhardy.afkstatus.Config;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.commands.arguments.EntityArgument;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AFKCommand {
    private static ChatFormatting getConfiguredColor() {
        try {
            return ChatFormatting.valueOf(Config.MESSAGE_COLOR.get().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return ChatFormatting.YELLOW;
        }
    }


    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("afk")
                        // Main /afk toggle command
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

                        // Subcommands under /afk blacklist ...
                        .then(Commands.literal("blacklist")
                                .requires(source -> source.hasPermission(2))

                                .then(Commands.literal("add")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> {
                                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                                    String name = target.getName().getString();
                                                    List<String> list = new ArrayList<>(BlacklistStorage.loadBlacklist());

                                                    if (!list.contains(name)) {
                                                        list.add(name);
                                                        BlacklistStorage.saveBlacklist(list);
                                                        AFKManager.reloadBlacklist();

                                                        ctx.getSource().sendSuccess(() -> Component.literal("Added " + name + " to AFK blacklist."), true);
                                                    } else {
                                                        ctx.getSource().sendFailure(Component.literal(name + " is already blacklisted."));
                                                    }
                                                    return 1;
                                                })
                                        )
                                )

                                .then(Commands.literal("remove")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> {
                                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                                    String name = target.getName().getString();
                                                    List<String> list = new ArrayList<>(BlacklistStorage.loadBlacklist());

                                                    if (list.remove(name)) {
                                                        BlacklistStorage.saveBlacklist(list);
                                                        AFKManager.reloadBlacklist();

                                                        ctx.getSource().sendSuccess(() -> Component.literal("Removed " + name + " from AFK blacklist."), true);
                                                    } else {
                                                        ctx.getSource().sendFailure(Component.literal(name + " is not in the blacklist."));
                                                    }
                                                    return 1;
                                                })
                                        )
                                )

                                .then(Commands.literal("list")
                                        .executes(ctx -> {
                                            List<String> list = BlacklistStorage.loadBlacklist();
                                            if (list.isEmpty()) {
                                                ctx.getSource().sendSuccess(() -> Component.literal("Blacklist is empty."), false);
                                            } else {
                                                ctx.getSource().sendSuccess(() -> Component.literal("Blacklisted: " + String.join(", ", list)), false);
                                            }
                                            return 1;
                                        })
                                )

                                .then(Commands.literal("reload")
                                        .executes(ctx -> {
                                            AFKManager.reloadBlacklist();
                                            ctx.getSource().sendSuccess(() -> Component.literal("AFK blacklist reloaded from disk."), true);
                                            return 1;
                                        })
                                )
                        )
        );
    }
}


