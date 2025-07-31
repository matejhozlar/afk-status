package com.saunhardy.afkstatus;

import com.mojang.logging.LogUtils;
import com.saunhardy.afkstatus.command.AFKCommand;
import org.slf4j.Logger;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(AFKStatus.MODID)
public class AFKStatus {

    public static final String MODID = "afkstatus";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AFKStatus(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        AFKCommand.register(event.getDispatcher());
    }

    public static void applyAFKTag(ServerPlayer player, boolean afk) {
        Scoreboard scoreboard = player.getServer().getScoreboard();
        String teamName = "afkstatus";

        PlayerTeam team = scoreboard.getPlayerTeam(teamName);
        if (team == null) {
            team = scoreboard.addPlayerTeam(teamName);
            team.setColor(ChatFormatting.GRAY);
            team.setPlayerPrefix(Component.literal("[AFK] ").withStyle(style -> style.withColor(ChatFormatting.GRAY)));
        }

        if (afk) {
            scoreboard.addPlayerToTeam(player.getScoreboardName(), team);
        } else {
            scoreboard.removePlayerFromTeam(player.getScoreboardName(), team);
        }
    }
}
