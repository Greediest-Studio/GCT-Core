package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

  public class BlockOrichalcosDreamwood extends Block {
  public static final Block block = new BlockOrichalcosDreamwood();

  public BlockOrichalcosDreamwood() {
    super(Material.ROCK);
    setTranslationKey("orichalcos_dreamwood");
    setSoundType(SoundType.WOOD);
    setHarvestLevel("axe", 8);
    setHardness(15.0F);
    setResistance(4000.0F);
    setLightLevel(1.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }
}
