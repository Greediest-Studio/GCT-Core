package com.smd.gctcore.common.items;

import com.smd.gctcore.common.botania.GctManaPoolTier;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemBlockGctManaTiered extends ItemBlock {

    public ItemBlockGctManaTiered(Block block) {
        super(block);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public int getMetadata(int damage) {
        return GctManaPoolTier.fromMeta(damage).ordinal();
    }

    @Override
    @Nonnull
    public String getTranslationKey(ItemStack stack) {
        return super.getTranslationKey(stack) + "." + GctManaPoolTier.fromMeta(stack.getMetadata()).getName();
    }
}
