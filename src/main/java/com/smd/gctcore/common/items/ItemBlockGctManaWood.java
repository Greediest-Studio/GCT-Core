package com.smd.gctcore.common.items;

import com.smd.gctcore.common.botania.GctManaWoodVariant;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemBlockGctManaWood extends ItemBlock {

    public ItemBlockGctManaWood(Block block) {
        super(block);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public int getMetadata(int damage) {
        return GctManaWoodVariant.fromMeta(damage).ordinal();
    }

    @Override
    @Nonnull
    public String getTranslationKey(ItemStack stack) {
        return super.getTranslationKey(stack) + "."
                + GctManaWoodVariant.fromMeta(stack.getMetadata()).getName();
    }
}
