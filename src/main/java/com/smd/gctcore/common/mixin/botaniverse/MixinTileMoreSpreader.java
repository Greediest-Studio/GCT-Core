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

    private static final int BURST_DELAY_TICKS = 20;
    private static final float BURST_ACTIVE_TICKS = 140.0F;

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

        // Botaniverse's normal spreaders pass 4.0 as their base motion
        // modifier.  That value is already applied to EntityManaBurst's
        // default motion; multiplying it by the tier value again makes the
        // top tiers several times too fast and causes receiver simulation to
        // fail.  Use the absolute tier modifier here.  Lens effects are
        // applied to BurstProperties immediately after this constructor, so
        // their motion changes remain intact.
        float effectiveMotion = motionModifier;
        if (isNILFHEIM_SPREADER()) {
            effectiveMotion = 2.25F;
        } else if (isMUSPELHEIM_SPREADER()) {
            effectiveMotion = 3.0F;
        } else if (isALFHEIM_SPREADER()) {
            effectiveMotion = 3.5F;
        } else if (isASGARD_SPREADER()) {
            effectiveMotion = 3.75F;
        }
        // Match the 20-tick delay + 140-tick active window used by GCT tiers.
        // This keeps Niflheim above Gaia and the remaining Botaniverse tiers
        // increasing in the same ten-tier progression.
        int effectiveDelay = BURST_DELAY_TICKS;
        float effectiveManaLoss = Math.max(1.0F, mana / BURST_ACTIVE_TICKS);
        return new BurstProperties(mana, effectiveDelay, effectiveManaLoss, gravity,
                effectiveMotion, color);
    }
}
