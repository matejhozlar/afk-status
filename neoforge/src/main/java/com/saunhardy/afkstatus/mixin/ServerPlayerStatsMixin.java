package com.saunhardy.afkstatus.mixin;

import com.saunhardy.afkstatus.AFKManager;
import com.saunhardy.afkstatus.Config;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerStatsMixin {

    @Inject(method = "awardStat(Lnet/minecraft/stats/Stat;I)V", at = @At("HEAD"), cancellable = true)
    private void afkstatus$pausePlayTimeWhileAfk(Stat<?> stat, int amount, CallbackInfo ci) {
        if (!Config.pausePlayTimeWhileAfk()
                || stat.getType() != Stats.CUSTOM
                || !Stats.PLAY_TIME.equals(stat.getValue())) {
            return;
        }
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (AFKManager.isAFK(player.getUUID())) {
            ci.cancel();
        }
    }
}
