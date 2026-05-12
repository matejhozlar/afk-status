package com.saunhardy.afkstatus.command;

import com.mojang.brigadier.CommandDispatcher;
import com.saunhardy.afkstatus.AFKManager;
import com.saunhardy.afkstatus.AFKStatus;
import com.saunhardy.afkstatus.Config;
import com.saunhardy.afkstatus.storage.BlacklistStorage;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AFKCommand {
    private static ChatFormatting getConfiguredColor() {
        try {
            return ChatFormatting.valueOf(Config.messageColor().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return ChatFormatting.YELLOW;
        }
    }

    private static void sendHelp(CommandSourceStack src) {
        boolean isOp = src.hasPermission(2);
        ChatFormatting accent = getConfiguredColor();

        src.sendSuccess(() ->
                        Component.literal("=== AFKStatus Commands ===")
                                .withStyle(s -> s.withBold(true).withColor(accent)),
                false
        );

        sendUsageLine(src, "/afk", "Toggle your AFK status on/off.", true);
        sendUsageLine(src, "/afk help", "Show help page.", true);

        if (isOp) {
            src.sendSuccess(() ->
                            Component.literal("")
                                    .append(Component.literal("Admin:").withStyle(s -> s.withBold(true).withColor(ChatFormatting.RED))),
                    false
            );

            sendUsageLine(src, "/afk blacklist add <player>", "Add a player to the AFK kick blacklist.", false);
            sendUsageLine(src, "/afk blacklist remove <player>", "Remove a player from the blacklist.", false);
            sendUsageLine(src, "/afk blacklist list", "Show all blacklisted names/UUIDs.", true);
            sendUsageLine(src, "/afk blacklist reload", "Reload blacklist from disk.", true);
        }

        src.sendSuccess(() ->
                        Component.literal("Tip: click a command to paste it into chat.")
                                .withStyle(s -> s.withItalic(true).withColor(ChatFormatting.GRAY)),
                false
        );
    }

    private static void sendUsageLine(CommandSourceStack src, String command, String description, boolean suggestOnClick) {
        MutableComponent bullet = Component.literal(" • ").withStyle(s -> s.withColor(getConfiguredColor()));
        ClickEvent click = suggestOnClick
                ? new ClickEvent.SuggestCommand(command)
                : new ClickEvent.CopyToClipboard(command);
        HoverEvent hover = new HoverEvent.ShowText(
                Component.literal(suggestOnClick ? "Click to paste" : "Click to copy"));
        MutableComponent cmd = Component.literal(command).setStyle(Style.EMPTY
                .withColor(ChatFormatting.GOLD)
                .withClickEvent(click)
                .withHoverEvent(hover));
        MutableComponent desc = Component.literal(" – " + description).withStyle(s -> s.withColor(ChatFormatting.GRAY));

        src.sendSuccess(() -> bullet.copy().append(cmd).append(desc), false);
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("afk")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayer();
                            if (player == null) {
                                context.getSource().sendFailure(Component.literal("You must be a player to use this command."));
                                return 0;
                            }
                            UUID uuid = player.getUUID();

                            boolean currentlyAFK = AFKManager.isAFK(uuid);
                            boolean newAFK = !currentlyAFK;

                            if (newAFK) {
                                AFKManager.setAFK(player, true);
                                AFKStatus.applyAFKTag(player, true);

                                if (Config.systemMessages()) {
                                    String msg = player.getName().getString() + " is now AFK.";
                                    var server = player.level().getServer();
                                    if (server != null) {
                                        server.getPlayerList().broadcastSystemMessage(
                                                Component.literal(msg).withStyle(style -> style.withColor(getConfiguredColor())),
                                                false
                                        );
                                    }
                                }
                            } else {
                                AFKManager.updateActivity(uuid, player);
                            }

                            return 1;
                        })

                        .then(Commands.literal("help")
                                .executes(ctx -> {
                                    sendHelp(ctx.getSource());
                                    return 1;
                                })
                        )

                        .then(Commands.literal("blacklist")
                                .requires(source -> source.hasPermission(2))

                                .then(Commands.literal("add")
                                        .then(Commands.argument("player", EntityArgument.player())
                                                .executes(ctx -> {
                                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                                    String name = target.getName().getString();
                                                    String nameLower = name.toLowerCase(Locale.ROOT);

                                                    List<String> list = new ArrayList<>(BlacklistStorage.loadBlacklist());
                                                    boolean alreadyBlacklisted = list.stream()
                                                            .map(s -> s.toLowerCase(Locale.ROOT))
                                                            .anyMatch(s -> s.equals(nameLower));

                                                    if (!alreadyBlacklisted) {
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
                                                    String nameLower = name.toLowerCase(Locale.ROOT);

                                                    List<String> list = new ArrayList<>(BlacklistStorage.loadBlacklist());
                                                    boolean removed = list.removeIf(s -> s.equalsIgnoreCase(nameLower));

                                                    if (removed) {
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
