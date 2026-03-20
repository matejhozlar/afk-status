package com.saunhardy.afkstatus.mixin;

import com.saunhardy.afkstatus.AFKManager;
import com.saunhardy.afkstatus.Config;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SleepStatus.class)
public class SleepStatusMixin {

    @Redirect(
            method = "update",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/server/level/ServerPlayer;isSpectator()Z")
    )
    private boolean afkstatus$redirectIsSpectator(ServerPlayer player) {
        if (Config.SLEEP_BYPASS_ENABLED.get() && AFKManager.isAFK(player.getUUID())) {
            return true;
        }
        return player.isSpectator();
    }
}
