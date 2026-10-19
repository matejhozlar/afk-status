package com.saunhardy.afkstatus.forge;

import com.saunhardy.afkstatus.platform.Services;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public final class ForgeServices implements Services {
    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean isFakePlayer(ServerPlayer player) {
        return player instanceof FakePlayer;
    }
}
