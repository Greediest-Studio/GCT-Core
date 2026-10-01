package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

  public class BlockCorruptrock extends Block {
  public static final Block block = new BlockCorruptrock();

  public BlockCorruptrock() {
    super(Material.ROCK);
    setTranslationKey("corruptrock");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 6);
    setHardness(5.0F);
    setResistance(20.0F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }
}
