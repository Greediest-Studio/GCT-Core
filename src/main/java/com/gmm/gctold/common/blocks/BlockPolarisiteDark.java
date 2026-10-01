package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

  public class BlockPolarisiteDark extends Block {
  public static final Block block = new BlockPolarisiteDark();

  public BlockPolarisiteDark() {
    super(Material.ROCK);
    setTranslationKey("polarisite_dark");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 8);
    setHardness(15.0F);
    setResistance(4000.0F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }
}
