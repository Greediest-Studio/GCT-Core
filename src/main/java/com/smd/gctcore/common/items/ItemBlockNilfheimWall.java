package com.smd.gctcore.common.items;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockNilfheimWall extends ItemBlock {
    public ItemBlockNilfheimWall(Block block) {
        super(block);
        setHasSubtypes(false);
        setMaxDamage(0);
    }

    @Override
    public int getMetadata(int damage) {
        return 0;
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return getTranslationKey();
    }
}
