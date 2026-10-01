package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
public class BlockOrderStoneFinal extends Block {
    public static final Block block = new BlockOrderStoneFinal();

    public BlockOrderStoneFinal() {
    super(Material.ROCK);
    this.setTranslationKey("order_stone_final");
    this.setSoundType(SoundType.GROUND);
    this.setHarvestLevel("pickaxe", 0);
    this.setHardness(-1.0f);
    this.setResistance(20000.0f);
    this.setLightLevel(0.0f);
    this.setLightOpacity(15);
    this.setCreativeTab(GctAllCreativeTab.TAB);

    }
}