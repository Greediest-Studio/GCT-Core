package com.smd.gctcore.common.mixin.industrialforegoing;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Pseudo
@Mixin(targets = "com.buuz135.industrial.utils.apihandlers.RecipeHandlers", remap = false)
public abstract class MixinRecipeHandlers {

    @ModifyConstant(method = "loadOreEntries", constant = @Constant(intValue = 150, ordinal = 0))
    private static int gctcore$boostOreFluidRawAmount(int original) {
        return 400;
    }
}
