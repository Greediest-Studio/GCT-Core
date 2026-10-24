package com.smd.gctcore.common.mixin.draconicevolution;

import com.shiver.gct_additions.common.entity.EntityOrderGuardian;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.brandon3055.draconicevolution.entity.EntityChaosGuardian", remap = false)
public class OrderGuardianBlockMixin {
    @Inject(method = "destroyBlocksInAABB", at = @At("HEAD"), cancellable = true, remap = false)
    private void gctcore$destroyOrderBlocks(AxisAlignedBB bounds, CallbackInfoReturnable<Boolean> callback) {
        if ((Object) this instanceof EntityOrderGuardian) {
            callback.setReturnValue(((EntityOrderGuardian) (Object) this).destroyOrderBlocks(bounds));
        }
    }
}
