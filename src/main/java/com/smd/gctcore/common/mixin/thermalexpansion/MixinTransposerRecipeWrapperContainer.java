package com.smd.gctcore.common.mixin.thermalexpansion;

import cofh.thermalexpansion.plugins.jei.machine.transposer.TransposerRecipeWrapper;
import com.smd.gctcore.common.integration.jei.TransposerContainerCache;
import mezz.jei.api.IGuiHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.Map;

@Pseudo
@Mixin(targets = "cofh.thermalexpansion.plugins.jei.machine.transposer.TransposerRecipeWrapperContainer", remap = false)
public abstract class MixinTransposerRecipeWrapperContainer extends TransposerRecipeWrapper {

    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/fluids/FluidRegistry;getRegisteredFluids()Ljava/util/Map;"), require = 2)
    private Map<String, Fluid> gctcore$skipDuplicateProbe() {
        return Collections.emptyMap();
    }

    @Inject(method = "<init>", at = @At("RETURN"), require = 1)
    private void gctcore$useSharedProbe(IGuiHelper guiHelper, ItemStack container, String uid, CallbackInfo ci) {
        TransposerContainerCache.Pair pair = TransposerContainerCache.get(container);
        boolean fill = "thermalexpansion.transposer_fill".equals(uid);
        TransposerContainerCache.Recipes recipes = fill ? pair.fill : pair.extract;
        this.inputs = Collections.singletonList(recipes.inputs);
        this.outputs = Collections.singletonList(recipes.outputs);
        this.inputFluids = fill ? Collections.singletonList(recipes.fluids) : Collections.emptyList();
        this.outputFluids = fill ? Collections.emptyList() : Collections.singletonList(recipes.fluids);
    }
}
