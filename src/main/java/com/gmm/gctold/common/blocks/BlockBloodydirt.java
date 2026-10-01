package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

  public class BlockBloodydirt extends Block {
  public static final Block block = new BlockBloodydirt();

  public BlockBloodydirt() {
    super(Material.GROUND);
    setTranslationKey("bloodydirt");
    setSoundType(SoundType.GROUND);
    setHarvestLevel("shovel", 6);
    setHardness(3.0F);
    setResistance(10.0F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }
}
