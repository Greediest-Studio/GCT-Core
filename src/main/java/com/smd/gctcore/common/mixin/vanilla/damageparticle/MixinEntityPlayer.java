package com.smd.gctcore.common.mixin.vanilla.damageparticle;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin({EntityPlayer.class})
public class MixinEntityPlayer {
    @Redirect(
            method = {"attackTargetEntityWithCurrentItem(Lnet/minecraft/entity/Entity;)V"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/WorldServer;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDIDDDD[I)V"
            )
    )
    private void inject_spawnParticle(WorldServer instance, EnumParticleTypes particleType, double xCoord, double yCoord, double zCoord, int numberOfParticles, double xOffset, double yOffset, double zOffset, double particleSpeed, int[] particleArguments) {
    }
}
