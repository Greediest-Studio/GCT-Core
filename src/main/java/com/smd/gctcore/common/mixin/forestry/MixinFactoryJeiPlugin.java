package com.smd.gctcore.common.mixin.forestry;

import com.smd.gctcore.common.integration.jei.JeiCategoryFilterHelper;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Removes Forestry's Bottler JEI recipe tab and skips generating its full
 * fluid x container recipe wrapper list.
 */
@Pseudo
@Mixin(targets = "forestry.factory.recipes.jei.FactoryJeiPlugin", remap = false)
public abstract class MixinFactoryJeiPlugin {

    @Unique
    private static final String BOTTLER_UID = "forestry.bottler";

    @Redirect(
            method = "registerCategories(Lmezz/jei/api/recipe/IRecipeCategoryRegistration;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lmezz/jei/api/recipe/IRecipeCategoryRegistration;addRecipeCategories([Lmezz/jei/api/recipe/IRecipeCategory;)V"
            ),
            remap = false,
            require = 1
    )
    private void gctcore$filterBottlerCategory(
            IRecipeCategoryRegistration registration,
            IRecipeCategory<?>[] categories
    ) {
        JeiCategoryFilterHelper.addFilteredCategories(registration, categories, BOTTLER_UID);
    }

    @Redirect(
            method = "register(Lmezz/jei/api/IModRegistry;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lforestry/factory/recipes/jei/bottler/BottlerRecipeMaker;getBottlerRecipes(Lmezz/jei/api/ingredients/IIngredientRegistry;)Ljava/util/List;"
            ),
            remap = false,
            require = 1
    )
    private List<?> gctcore$skipBottlerRecipeGeneration(IIngredientRegistry ingredientRegistry) {
        return Collections.emptyList();
    }

    @Redirect(
            method = "register(Lmezz/jei/api/IModRegistry;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lmezz/jei/api/IModRegistry;addRecipes(Ljava/util/Collection;Ljava/lang/String;)V"
            ),
            remap = false,
            require = 1
    )
    private void gctcore$skipBottlerRecipes(
            IModRegistry registry,
            Collection<?> recipes,
            String categoryUid
    ) {
        JeiCategoryFilterHelper.addRecipesExcept(registry, recipes, categoryUid, BOTTLER_UID);
    }

    @Redirect(
            method = "register(Lmezz/jei/api/IModRegistry;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lmezz/jei/api/IModRegistry;addRecipeClickArea(Ljava/lang/Class;IIII[Ljava/lang/String;)V"
            ),
            remap = false,
            require = 1
    )
    private void gctcore$skipBottlerClickArea(
            IModRegistry registry,
            Class<? extends GuiContainer> guiContainerClass,
            int xPos,
            int yPos,
            int width,
            int height,
            String[] categoryUids
    ) {
        JeiCategoryFilterHelper.addFilteredClickArea(
                registry, guiContainerClass, xPos, yPos, width, height, categoryUids, BOTTLER_UID
        );
    }

    @Redirect(
            method = "register(Lmezz/jei/api/IModRegistry;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lmezz/jei/api/IModRegistry;addRecipeCatalyst(Ljava/lang/Object;[Ljava/lang/String;)V"
            ),
            remap = false,
            require = 1
    )
    private void gctcore$skipBottlerCatalyst(
            IModRegistry registry,
            Object catalystIngredient,
            String[] categoryUids
    ) {
        JeiCategoryFilterHelper.addFilteredCatalyst(registry, catalystIngredient, categoryUids, BOTTLER_UID);
    }
}
