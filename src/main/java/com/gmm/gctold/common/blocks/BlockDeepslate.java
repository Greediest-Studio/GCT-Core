package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
public class BlockDeepslate extends Block {
    public static final Block block = new BlockDeepslate();

    public BlockDeepslate() {
    super(Material.ROCK);
    this.setTranslationKey("deepslate");
    this.setSoundType(SoundType.GROUND);
    this.setHarvestLevel("pickaxe", 0);
    this.setHardness(3.0f);
    this.setResistance(6.0f);
    this.setLightLevel(0.0f);
    this.setLightOpacity(15);
    this.setCreativeTab(GctAllCreativeTab.TAB);

    }
}