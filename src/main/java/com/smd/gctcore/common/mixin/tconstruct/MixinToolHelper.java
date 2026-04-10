package com.smd.gctcore.common.mixin.tconstruct;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import slimeknights.tconstruct.library.utils.ToolHelper;

@Mixin(ToolHelper.class)
public class MixinToolHelper {

    @Redirect(
            method = "attackEntity(Lnet/minecraft/item/ItemStack;Lslimeknights/tconstruct/library/tools/ToolCore;Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity;Z)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/WorldServer;func_175739_a(Lnet/minecraft/util/EnumParticleTypes;DDDIDDDD[I)V"
            ),
            remap = false
    )
    private static void cancelDamageParticles(
            WorldServer worldServer,
            EnumParticleTypes particleType,
            double x,
            double y,
            double z,
            int particleCount,
            double xOffset,
            double yOffset,
            double zOffset,
            double particleSpeed,
            int[] parameters
    ) {
    }
}
