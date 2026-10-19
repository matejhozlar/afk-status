package com.saunhardy.afkstatus.neoforge;

import com.saunhardy.afkstatus.platform.Services;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.util.FakePlayer;

import java.nio.file.Path;

public final class NeoForgeServices implements Services {
    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean isFakePlayer(ServerPlayer player) {
        return player instanceof FakePlayer;
    }
}
