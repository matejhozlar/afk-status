package com.saunhardy.afkstatus.forge;

import com.saunhardy.afkstatus.platform.Services;

import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public final class ForgeServices implements Services {
    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }
}
