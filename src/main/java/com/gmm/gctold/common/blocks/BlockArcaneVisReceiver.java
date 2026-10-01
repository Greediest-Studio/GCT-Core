package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

  public class BlockArcaneVisReceiver extends Block {
  public static final Block block = new BlockArcaneVisReceiver();

  public BlockArcaneVisReceiver() {
    super(Material.ROCK);
    setTranslationKey("arcane_vis_receiver");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 1);
    setHardness(1.5F);
    setResistance(10.0F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }
}
