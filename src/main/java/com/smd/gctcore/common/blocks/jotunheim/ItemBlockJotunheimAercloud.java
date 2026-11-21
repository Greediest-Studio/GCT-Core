package com.smd.gctcore.common.blocks.jotunheim;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockJotunheimAercloud extends ItemBlock {
    private static final String[] NAMES = {"frostbound", "ashen", "thundering"};

    public ItemBlockJotunheimAercloud(BlockJotunheimAercloud block) {
        super(block);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public int getMetadata(int damage) { return damage >= 0 && damage < 3 ? damage : 0; }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return "tile.gctcore.jotunheim_aercloud." + NAMES[getMetadata(stack.getMetadata())];
    }
}
