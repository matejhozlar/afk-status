package com.saunhardy.afkstatus.neoforge;

import com.saunhardy.afkstatus.platform.Services;

import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public final class NeoForgeServices implements Services {
    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }
}
