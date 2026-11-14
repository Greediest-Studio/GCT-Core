package com.smd.gctcore.common.integration.jei;

import mezz.jei.api.IModRegistry;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.IRecipeWrapperFactory;
import net.minecraft.client.gui.inventory.GuiContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Shared helpers for JEI plugin mixins that omit specific heavy recipe categories
 * before JEI builds its recipe tables.
 */
public final class JeiCategoryFilterHelper {

    private JeiCategoryFilterHelper() {
    }

    public static void addFilteredCategories(
            IRecipeCategoryRegistration registration,
            IRecipeCategory<?>[] categories,
            String excludedUid
    ) {
        List<IRecipeCategory<?>> filtered = new ArrayList<>(categories.length);
        for (IRecipeCategory<?> category : categories) {
            if (category != null && !excludedUid.equals(category.getUid())) {
                filtered.add(category);
            }
        }
        if (!filtered.isEmpty()) {
            registration.addRecipeCategories(filtered.toArray(new IRecipeCategory<?>[0]));
        }
    }

    public static void addRecipesExcept(
            IModRegistry registry,
            Collection<?> recipes,
            String categoryUid,
            String excludedUid
    ) {
        if (!excludedUid.equals(categoryUid)) {
            registry.addRecipes(recipes, categoryUid);
        }
    }

    public static <T> void handleRecipesExcept(
            IModRegistry registry,
            Class<T> recipeClass,
            IRecipeWrapperFactory<T> factory,
            String categoryUid,
            String excludedUid
    ) {
        if (!excludedUid.equals(categoryUid)) {
            registry.handleRecipes(recipeClass, factory, categoryUid);
        }
    }

    public static void addFilteredClickArea(
            IModRegistry registry,
            Class<? extends GuiContainer> guiContainerClass,
            int xPos,
            int yPos,
            int width,
            int height,
            String[] categoryUids,
            String excludedUid
    ) {
        String[] filtered = filterUids(categoryUids, excludedUid);
        if (filtered.length > 0) {
            registry.addRecipeClickArea(guiContainerClass, xPos, yPos, width, height, filtered);
        }
    }

    public static void addFilteredCatalyst(
            IModRegistry registry,
            Object catalystIngredient,
            String[] categoryUids,
            String excludedUid
    ) {
        String[] filtered = filterUids(categoryUids, excludedUid);
        if (filtered.length > 0) {
            registry.addRecipeCatalyst(catalystIngredient, filtered);
        }
    }

    private static String[] filterUids(String[] categoryUids, String excludedUid) {
        List<String> filtered = new ArrayList<>(categoryUids.length);
        for (String uid : categoryUids) {
            if (!excludedUid.equals(uid)) {
                filtered.add(uid);
            }
        }
        return filtered.toArray(new String[0]);
    }
}
