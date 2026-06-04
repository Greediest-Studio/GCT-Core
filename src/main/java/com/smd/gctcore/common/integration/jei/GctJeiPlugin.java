package com.smd.gctcore.common.integration.jei;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.integration.jei.quartz.QuartzCategory;
import com.smd.gctcore.common.integration.jei.quartz.QuartzWrapper;
import com.smd.gctcore.misc.BlockRegistry;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collections;

@JEIPlugin
public class GctJeiPlugin implements IModPlugin {
    public static final String RADIANT_RESONATOR = Tags.MOD_ID + ".radiant_resonator";

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        IGuiHelper helper = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(new QuartzCategory(helper));
    }

    @Override
    public void register(@Nonnull IModRegistry registry) {
        registry.handleRecipes(QuartzWrapper.FakeQuartzRecipe.class, QuartzWrapper::new, RADIANT_RESONATOR);
        registry.addRecipes(Collections.singletonList(new QuartzWrapper.FakeQuartzRecipe()), RADIANT_RESONATOR);
        registry.addRecipeCatalyst(new ItemStack(BlockRegistry.RADIANT_RESONATOR), RADIANT_RESONATOR);
    }
}
