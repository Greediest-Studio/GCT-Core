package com.smd.gctcore.common.mixin.hei;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import mezz.jei.api.recipe.IIngredientType;
import mezz.jei.ingredients.Ingredients;
import mezz.jei.recipes.RecipeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.Map;

@Mixin(value = RecipeRegistry.class, remap = false)
public abstract class MixinRecipeRegistry {

    // calculateId 会逐类型读取原料；每次计算只展开一次整份表，保持原来的哈希顺序。
    @Redirect(method = "calculateId", at = @At(value = "INVOKE",
            target = "Lmezz/jei/ingredients/Ingredients;getInputIngredients()Ljava/util/Map;"), require = 1)
    private Map<IIngredientType, List> gctcore$reuseInputs(Ingredients ingredients,
            @Share("inputIngredients") LocalRef<Map<IIngredientType, List>> cached) {
        if (cached.get() == null) {
            cached.set(ingredients.getInputIngredients());
        }
        return cached.get();
    }

    @Redirect(method = "calculateId", at = @At(value = "INVOKE",
            target = "Lmezz/jei/ingredients/Ingredients;getOutputIngredients()Ljava/util/Map;"), require = 1)
    private Map<IIngredientType, List> gctcore$reuseOutputs(Ingredients ingredients,
            @Share("outputIngredients") LocalRef<Map<IIngredientType, List>> cached) {
        if (cached.get() == null) {
            cached.set(ingredients.getOutputIngredients());
        }
        return cached.get();
    }
}
