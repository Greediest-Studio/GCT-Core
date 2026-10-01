package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

  public class BlockReversedAlfDirt extends Block {
  public static final Block block = new BlockReversedAlfDirt();

  public BlockReversedAlfDirt() {
    super(Material.GROUND);
    setTranslationKey("reversed_alf_dirt");
    setSoundType(SoundType.GROUND);
    setHarvestLevel("shovel", 1);
    setHardness(0.5F);
    setResistance(0.5F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }
}
