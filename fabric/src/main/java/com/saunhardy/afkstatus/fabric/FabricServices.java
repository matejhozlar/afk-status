package com.saunhardy.afkstatus.fabric;

import com.saunhardy.afkstatus.platform.Services;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public final class FabricServices implements Services {
    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
