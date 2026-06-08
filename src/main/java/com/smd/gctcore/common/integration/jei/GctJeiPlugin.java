package com.smd.gctcore.common.integration.jei;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.integration.jei.quartz.QuartzCategory;
import com.smd.gctcore.common.integration.jei.quartz.QuartzWrapper;
import com.smd.gctcore.common.items.bloodmagic.soulgem.SoulGem;
import com.smd.gctcore.misc.BlockRegistry;
import com.smd.gctcore.misc.ItemRegistry;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import WayofTime.bloodmagic.soul.EnumDemonWillType;
import WayofTime.bloodmagic.util.Constants;

import javax.annotation.Nonnull;
import java.util.Collections;

@JEIPlugin
public class GctJeiPlugin implements IModPlugin {
    public static final String RADIANT_RESONATOR = Tags.MOD_ID + ".radiant_resonator";

    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
        subtypeRegistry.registerSubtypeInterpreter(ItemRegistry.ITEM_SOUL_GEM, GctJeiPlugin::getSoulGemSubtype);
    }

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
        hideNonRawSoulGems(registry);
    }

    private static String getSoulGemSubtype(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        String willType = EnumDemonWillType.DEFAULT.toString();
        double souls = 0;
        if (tag != null) {
            if (tag.hasKey(Constants.NBT.WILL_TYPE)) {
                willType = tag.getString(Constants.NBT.WILL_TYPE);
            }
            if (tag.hasKey(Constants.NBT.SOULS)) {
                souls = tag.getDouble(Constants.NBT.SOULS);
            }
        }
        return stack.getMetadata() + ":" + willType + ":" + Double.toString(souls);
    }

    private static void hideNonRawSoulGems(IModRegistry registry) {
        if (!(ItemRegistry.ITEM_SOUL_GEM instanceof SoulGem)) {
            return;
        }

        SoulGem soulGem = (SoulGem) ItemRegistry.ITEM_SOUL_GEM;
        IIngredientBlacklist blacklist = registry.getJeiHelpers().getIngredientBlacklist();
        for (EnumDemonWillType type : EnumDemonWillType.values()) {
            if (type == EnumDemonWillType.DEFAULT) {
                continue;
            }
            for (int i = 0; i < SoulGem.names.length; i++) {
                ItemStack stack = new ItemStack(soulGem, 1, i);
                soulGem.setWill(type, stack, soulGem.getMaxWill(EnumDemonWillType.DEFAULT, stack));
                blacklist.addIngredientToBlacklist(stack);
            }
        }
    }
}
