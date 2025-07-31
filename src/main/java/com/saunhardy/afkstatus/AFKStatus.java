package com.saunhardy.afkstatus;

import com.mojang.logging.LogUtils;
import com.saunhardy.afkstatus.command.AFKCommand;
import org.slf4j.Logger;

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
}