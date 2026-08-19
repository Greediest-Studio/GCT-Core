package com.smd.gctcore.common.mixin.botaniverse;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.botania.api.mana.BurstProperties;

@Pseudo
@Mixin(targets = "com.aeternal.botaniverse.common.block.tile.TileMoreSpreader", remap = false)
public abstract class MixinTileMoreSpreader {

    @Shadow public abstract boolean isNILFHEIM_SPREADER();
    @Shadow public abstract boolean isMUSPELHEIM_SPREADER();
    @Shadow public abstract boolean isALFHEIM_SPREADER();
    @Shadow public abstract boolean isASGARD_SPREADER();

    @Inject(method = "getMaxMana", at = @At("HEAD"), cancellable = true, require = 1)
    private void gctcore$replaceSpreaderCapacity(CallbackInfoReturnable<Integer> cir) {
        if (isNILFHEIM_SPREADER()) {
            cir.setReturnValue(25_000);
        } else if (isMUSPELHEIM_SPREADER()) {
            cir.setReturnValue(1_600_000);
        } else if (isALFHEIM_SPREADER()) {
            cir.setReturnValue(25_000_000);
        } else if (isASGARD_SPREADER()) {
            cir.setReturnValue(100_000_000);
        }
    }

    @Redirect(
            method = "getBurst",
            at = @At(value = "NEW", target = "vazkii.botania.api.mana.BurstProperties"),
            require = 1
    )
    private BurstProperties gctcore$replaceBurstMana(int originalMana, int ticksBeforeManaLoss,
                                                      float manaLossPerTick, float gravity,
                                                      float motionModifier, int color) {
        int mana = originalMana;
        if (isNILFHEIM_SPREADER()) {
            mana = 1_600;
        } else if (isMUSPELHEIM_SPREADER()) {
            mana = 100_000;
        } else if (isALFHEIM_SPREADER()) {
            mana = 1_600_000;
        } else if (isASGARD_SPREADER()) {
            mana = 6_400_000;
        }

        // Botaniverse passes 0 for motionModifier in every normal tier, which
        // zeroes EntityManaBurst's default 0.4 block/tick motion.  Replace it
        // with the same seven-tier progression used by GCT-Core.  A non-zero
        // value supplied by a lens is retained as a multiplier.
        float tierMotion = 1.0F;
        if (isMUSPELHEIM_SPREADER()) {
            tierMotion = 1.75F;
        } else if (isALFHEIM_SPREADER()) {
            tierMotion = 2.25F;
        } else if (isASGARD_SPREADER()) {
            tierMotion = 2.5F;
        }
        float effectiveMotion = motionModifier == 0.0F
                ? tierMotion : tierMotion * motionModifier;
        return new BurstProperties(mana, ticksBeforeManaLoss, manaLossPerTick, gravity,
                effectiveMotion, color);
    }
}
