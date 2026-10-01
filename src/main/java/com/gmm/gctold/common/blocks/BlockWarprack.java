package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

  public class BlockWarprack extends Block {
  public static final Block block = new BlockWarprack();

  public BlockWarprack() {
    super(Material.ROCK);
    setTranslationKey("warprack");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 10);
    setHardness(15.0F);
    setResistance(4000.0F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }
}
