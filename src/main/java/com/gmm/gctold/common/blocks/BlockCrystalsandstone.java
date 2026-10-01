package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

  public class BlockCrystalsandstone extends Block {
  public static final Block block = new BlockCrystalsandstone();

  public BlockCrystalsandstone() {
    super(Material.ROCK);
    setTranslationKey("crystalsandstone");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 0);
    setHardness(5.0F);
    setResistance(10.0F);
    setLightLevel(0.6666667F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }
}
