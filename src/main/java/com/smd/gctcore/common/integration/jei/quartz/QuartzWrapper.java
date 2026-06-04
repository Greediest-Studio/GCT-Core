package com.smd.gctcore.common.integration.jei.quartz;

import com.smd.gctcore.common.config.GctCoreConfig;
import com.smd.gctcore.misc.ItemRegistry;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import java.awt.Color;

public class QuartzWrapper implements IRecipeWrapper {
    public QuartzWrapper(FakeQuartzRecipe recipe) {
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setOutput(VanillaTypes.ITEM, new ItemStack(ItemRegistry.RAW_QUARTZ));
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        double minutes = GctCoreConfig.radiantResonator.resonatorTickTime / 20.0D / 60.0D;
        String info = I18n.format("jei.gctcore.resonator", minutes);
        minecraft.fontRenderer.drawString(info, 0, 66, Color.BLACK.getRGB());
    }

    public static class FakeQuartzRecipe {
    }
}
