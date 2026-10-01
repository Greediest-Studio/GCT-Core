package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

  public class BlockAlfSand extends BlockFalling {
  public static final Block block = new BlockAlfSand();

  public BlockAlfSand() {
    super(Material.SAND);
    setTranslationKey("alf_sand");
    setSoundType(SoundType.SAND);
    setHarvestLevel("shovel", 0);
    setHardness(0.5F);
    setResistance(0.5F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }
}
