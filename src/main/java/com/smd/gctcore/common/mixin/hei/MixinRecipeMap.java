package com.smd.gctcore.common.mixin.hei;

import com.llamalad7.mixinextras.sugar.Local;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.collect.ListMultiMap;
import mezz.jei.recipes.RecipeMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(value = RecipeMap.class, remap = false)
public abstract class MixinRecipeMap {

    @Shadow @Final
    private ListMultiMap<String, String> categoryUidMap;

    @Redirect(method = "addRecipe(Lmezz/jei/api/recipe/IRecipeWrapper;Lmezz/jei/api/recipe/IRecipeCategory;Lmezz/jei/api/recipe/IIngredientType;Ljava/util/List;)V",
            at = @At(value = "INVOKE", target = "Lmezz/jei/recipes/RecipeMap;addRecipeCategory(Lmezz/jei/api/recipe/IRecipeCategory;Ljava/lang/Object;Lmezz/jei/api/ingredients/IIngredientHelper;)V"), require = 1)
    private void gctcore$reuseIngredientId(RecipeMap recipeMap, IRecipeCategory category,
            Object ingredient, IIngredientHelper helper, @Local String key) {
        // 原料索引刚算过 key，分类索引直接复用，保留分类去重与顺序。
        List<String> categories = categoryUidMap.get(key);
        String categoryUid = category.getUid();
        if (!categories.contains(categoryUid)) {
            categories.add(categoryUid);
        }
    }
}
