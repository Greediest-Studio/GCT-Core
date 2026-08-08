package com.smd.gctcore.common.integration.extendedcrafting;

import com.smd.gctcore.Tags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemBlankExtendedPattern extends Item {
    private final ExtendedCraftingTier tier;

    public ItemBlankExtendedPattern(ExtendedCraftingTier tier) {
        this.tier = tier;
        setRegistryName(Tags.MOD_ID, tier.id() + "_blank_extended_pattern");
        setTranslationKey(Tags.MOD_ID + "." + tier.id() + "_blank_extended_pattern");
        setCreativeTab(CreativeTabs.MISC);
        setMaxStackSize(64);
    }

    public ExtendedCraftingTier tier() {
        return tier;
    }
}
