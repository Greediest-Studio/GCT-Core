package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

  public class BlockSeekBrick extends Block {
  public static final Block block = new BlockSeekBrick();

  public BlockSeekBrick() {
    super(Material.ROCK);
    setTranslationKey("seek_brick");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 13);
    setHardness(250.0F);
    setResistance(1.0E7F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }
}
