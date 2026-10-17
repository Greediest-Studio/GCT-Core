package com.smd.gctcore.common.integration.astralsorcery;

import hellfirepvp.astralsorcery.common.block.network.BlockCollectorCrystalBase;
import hellfirepvp.astralsorcery.common.item.block.ItemCollectorCrystal;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;

/** Item form of the eternal collector crystal. */
public class EternalCollectorCrystalItem extends ItemCollectorCrystal {

    public EternalCollectorCrystalItem(EternalCollectorCrystalBlock block) {
        super(block);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return AstralSorceryExpansion.ETERNAL_RARITY;
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return 1;
    }
}
