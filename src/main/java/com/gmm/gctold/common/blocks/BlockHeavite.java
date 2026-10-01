package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

  public class BlockHeavite extends Block {
  public static final Block block = new BlockHeavite();

  public BlockHeavite() {
    super(Material.ROCK);
    setTranslationKey("heavite");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 11);
    setHardness(50.0F);
    setResistance(4000.0F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }
}
