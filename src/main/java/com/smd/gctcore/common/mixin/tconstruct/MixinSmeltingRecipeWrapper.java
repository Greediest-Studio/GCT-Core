package com.smd.gctcore.common.mixin.tconstruct;

import com.smd.gctcore.common.integration.jei.TConJeiFuelCache;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.tconstruct.library.smeltery.MeltingRecipe;
import slimeknights.tconstruct.plugin.jei.smelting.SmeltingRecipeWrapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Avoids cloning all smeltery fuels per MeltingRecipe in JEI.
 * HighOvenMeltingWrapper does not display liquid fuels in its category at all.
 */
@Mixin(value = SmeltingRecipeWrapper.class, remap = false)
public abstract class MixinSmeltingRecipeWrapper {

    private static final String HIGH_OVEN_WRAPPER_CLASS =
            "knightminer.tcomplement.plugin.jei.highoven.melting.HighOvenMeltingWrapper";

    @Shadow
    @Final
    @Mutable
    protected List<FluidStack> fuels;

    @Shadow
    protected int temperature;

    @Redirect(
            method = "<init>(Lslimeknights/tconstruct/library/smeltery/MeltingRecipe;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lslimeknights/tconstruct/library/TinkerRegistry;getSmelteryFuels()Ljava/util/Collection;"
            ),
            remap = false
    )
    private Collection<FluidStack> gctcore$skipPerRecipeFuelClone() {
        return Collections.emptyList();
    }

    @Inject(
            method = "<init>(Lslimeknights/tconstruct/library/smeltery/MeltingRecipe;)V",
            at = @At("RETURN"),
            remap = false
    )
    private void gctcore$assignSharedFuels(MeltingRecipe recipe, CallbackInfo ci) {
        if (HIGH_OVEN_WRAPPER_CLASS.equals(((Object) this).getClass().getName())) {
            this.fuels = Collections.emptyList();
            return;
        }
        this.fuels = TConJeiFuelCache.getSharedFuels(this.temperature);
    }
}
