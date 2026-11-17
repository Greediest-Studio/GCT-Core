package com.smd.gctcore.common.mixin.hei;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.smd.gctcore.common.integration.jei.RecipeStageOutputIndex;
import crafttweaker.api.item.IItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Pseudo
@Mixin(targets = "com.blamejared.recipestages.handlers.Recipes$ActionSetOutputStages", remap = false)
public abstract class MixinRecipeStages {
    @Redirect(method = "apply()V", at = @At(value = "INVOKE",
            target = "Ljava/util/Map;entrySet()Ljava/util/Set;"), require = 1)
    private Set<Map.Entry<String, List<IItemStack>>> gctcore$findOutputStages(
            Map<String, List<IItemStack>> outputs, @Local IItemStack stack,
            @Share("outputIndex") LocalRef<RecipeStageOutputIndex> cached) {
        if (cached.get() == null) {
            cached.set(new RecipeStageOutputIndex(outputs));
        }
        return cached.get().getCandidates(stack);
    }
}
