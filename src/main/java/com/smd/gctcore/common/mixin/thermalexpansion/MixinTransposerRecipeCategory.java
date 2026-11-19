package com.smd.gctcore.common.mixin.thermalexpansion;

import cofh.thermalexpansion.plugins.jei.RecipeUidsTE;
import com.smd.gctcore.common.integration.jei.JeiCategoryFilterHelper;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "cofh.thermalexpansion.plugins.jei.machine.transposer.TransposerRecipeCategory", remap = false)
public abstract class MixinTransposerRecipeCategory {

    @Redirect(method = "register", at = @At(value = "INVOKE",
            target = "Lmezz/jei/api/recipe/IRecipeCategoryRegistration;addRecipeCategories([Lmezz/jei/api/recipe/IRecipeCategory;)V"), require = 1)
    private static void gctcore$filterExtractCategory(IRecipeCategoryRegistration registration, IRecipeCategory<?>[] categories) {
        JeiCategoryFilterHelper.addFilteredCategories(registration, categories, RecipeUidsTE.TRANSPOSER_EXTRACT);
    }

    @Redirect(method = "initialize", at = @At(value = "INVOKE",
            target = "Lcofh/thermalexpansion/plugins/jei/machine/transposer/TransposerRecipeCategoryExtract;initialize(Lmezz/jei/api/IModRegistry;)V"), require = 1)
    private static void gctcore$skipExtract(IModRegistry registry) {
        // This call generates all extract wrappers and registers their machine catalyst.
    }

    @Redirect(method = "initialize", at = @At(value = "INVOKE",
            target = "Lmezz/jei/api/IModRegistry;addRecipeClickArea(Ljava/lang/Class;IIII[Ljava/lang/String;)V"), require = 1)
    private static void gctcore$filterClickArea(IModRegistry registry, Class<? extends GuiContainer> gui,
                                                int x, int y, int width, int height, String[] uids) {
        JeiCategoryFilterHelper.addFilteredClickArea(registry, gui, x, y, width, height, uids, RecipeUidsTE.TRANSPOSER_EXTRACT);
    }
}
