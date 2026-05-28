package com.smd.gctcore.common.integration.jei.quartz;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.integration.jei.GctJeiPlugin;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class QuartzCategory implements IRecipeCategory<QuartzWrapper> {
    private final IDrawable background;

    public QuartzCategory(IGuiHelper helper) {
        background = helper.createDrawable(new ResourceLocation(Tags.MOD_ID, "textures/gui/jei/radiant_resonator.png"), 0, 0, 22, 62);
    }

    @Override
    public String getUid() {
        return GctJeiPlugin.RADIANT_RESONATOR;
    }

    @Override
    public String getTitle() {
        return I18n.format("tile.gctcore.radiant_resonator.name");
    }

    @Override
    public String getModName() {
        return Tags.MOD_NAME;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, QuartzWrapper recipeWrapper, IIngredients ingredients) {
        recipeLayout.getItemStacks().init(0, false, 2, 22);
        recipeLayout.getItemStacks().set(0, ingredients.getOutputs(VanillaTypes.ITEM).get(0));
    }
}
