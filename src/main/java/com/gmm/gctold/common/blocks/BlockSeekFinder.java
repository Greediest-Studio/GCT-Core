package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

  public class BlockSeekFinder extends Block {
  public static final Block block = new BlockSeekFinder();

  public BlockSeekFinder() {
    super(Material.ROCK);
    setTranslationKey("seek_finder");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 20);
    setHardness(500.0F);
    setResistance(1.0E7F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }
}
