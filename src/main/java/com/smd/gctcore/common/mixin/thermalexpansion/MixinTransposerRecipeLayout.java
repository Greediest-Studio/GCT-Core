package com.smd.gctcore.common.mixin.thermalexpansion;

import cofh.thermalexpansion.plugins.jei.RecipeUidsTE;
import cofh.thermalexpansion.plugins.jei.machine.transposer.TransposerRecipeWrapperContainer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.smd.gctcore.common.integration.jei.TransposerFillPreview;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.gui.recipes.RecipeLayout;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = RecipeLayout.class, remap = false)
public abstract class MixinTransposerRecipeLayout {
    // Recipe registration still calls getIngredients directly and indexes every variant.
    @WrapOperation(method = "create", at = @At(value = "INVOKE",
            target = "Lmezz/jei/api/recipe/IRecipeWrapper;getIngredients(Lmezz/jei/api/ingredients/IIngredients;)V"), require = 1)
    private static void gctcore$writeDisplayIngredients(IRecipeWrapper wrapper, IIngredients ingredients, Operation<Void> original,
            @Local(argsOnly = true) IRecipeCategory<?> category, @Local(argsOnly = true) IFocus<?> focus) {
        if (wrapper instanceof TransposerRecipeWrapperContainer && RecipeUidsTE.TRANSPOSER_FILL.equals(category.getUid())) {
            TransposerIngredientsAccessor rows = (TransposerIngredientsAccessor) wrapper;
            if (TransposerFillPreview.write(ingredients, focus, rows.gctcore$getInputs(), rows.gctcore$getOutputs(), rows.gctcore$getInputFluids())) {
                return;
            }
        }
        original.call(wrapper, ingredients);
    }
}
