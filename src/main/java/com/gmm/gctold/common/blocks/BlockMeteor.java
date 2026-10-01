package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

  public class BlockMeteor extends Block {
  public static final Block block = new BlockMeteor();

  public BlockMeteor() {
    super(Material.ROCK);
    setTranslationKey("meteor");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 9);
    setHardness(5.0F);
    setResistance(4000.0F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }
}
