package com.smd.gctcore.common.mixin.smelteryio;

import com.smd.gctcore.common.integration.jei.JeiCategoryFilterHelper;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.IRecipeWrapperFactory;
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
 * Removes SmelteryIO's Casting Machine JEI recipe tab and skips generating its
 * duplicate casting recipe wrappers while keeping Fuel Controller intact.
 */
@Pseudo
@Mixin(targets = "mctmods.smelteryio.util.jei.JEI", remap = false)
public abstract class MixinSmelteryIoJei {

    @Unique
    private static final String CASTING_MACHINE_UID = "smelteryio:casting_machine";

    @Redirect(
            method = "registerCategories(Lmezz/jei/api/recipe/IRecipeCategoryRegistration;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lmezz/jei/api/recipe/IRecipeCategoryRegistration;addRecipeCategories([Lmezz/jei/api/recipe/IRecipeCategory;)V"
            ),
            remap = false,
            require = 1
    )
    private void gctcore$filterCastingMachineCategory(
            IRecipeCategoryRegistration registration,
            IRecipeCategory<?>[] categories
    ) {
        JeiCategoryFilterHelper.addFilteredCategories(registration, categories, CASTING_MACHINE_UID);
    }

    @Redirect(
            method = "register(Lmezz/jei/api/IModRegistry;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lmctmods/smelteryio/util/jei/CMRecipeChecker;getCastingRecipes(Lmezz/jei/api/gui/IDrawable;Lmezz/jei/api/gui/IDrawable;)Ljava/util/List;"
            ),
            remap = false,
            require = 1
    )
    private List<?> gctcore$skipCastingRecipeGeneration(IDrawable arrowFull, IDrawable arrowEmpty) {
        return Collections.emptyList();
    }

    @Redirect(
            method = "register(Lmezz/jei/api/IModRegistry;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lmezz/jei/api/IModRegistry;handleRecipes(Ljava/lang/Class;Lmezz/jei/api/recipe/IRecipeWrapperFactory;Ljava/lang/String;)V"
            ),
            remap = false,
            require = 1
    )
    private <T> void gctcore$skipCastingRecipeHandler(
            IModRegistry registry,
            Class<T> recipeClass,
            IRecipeWrapperFactory<T> factory,
            String categoryUid
    ) {
        JeiCategoryFilterHelper.handleRecipesExcept(registry, recipeClass, factory, categoryUid, CASTING_MACHINE_UID);
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
    private void gctcore$skipCastingRecipes(
            IModRegistry registry,
            Collection<?> recipes,
            String categoryUid
    ) {
        JeiCategoryFilterHelper.addRecipesExcept(registry, recipes, categoryUid, CASTING_MACHINE_UID);
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
    private void gctcore$skipCastingClickArea(
            IModRegistry registry,
            Class<? extends GuiContainer> guiContainerClass,
            int xPos,
            int yPos,
            int width,
            int height,
            String[] categoryUids
    ) {
        JeiCategoryFilterHelper.addFilteredClickArea(
                registry, guiContainerClass, xPos, yPos, width, height, categoryUids, CASTING_MACHINE_UID
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
    private void gctcore$skipCastingCatalyst(
            IModRegistry registry,
            Object catalystIngredient,
            String[] categoryUids
    ) {
        JeiCategoryFilterHelper.addFilteredCatalyst(registry, catalystIngredient, categoryUids, CASTING_MACHINE_UID);
    }
}
