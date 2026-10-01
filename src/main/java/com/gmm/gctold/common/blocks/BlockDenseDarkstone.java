package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

  public class BlockDenseDarkstone extends Block {
  public static final Block block = new BlockDenseDarkstone();

  public BlockDenseDarkstone() {
    super(Material.ROCK);
    setTranslationKey("densedarkstone");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 8);
    setHardness(3.0F);
    setResistance(4000.0F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }
}
