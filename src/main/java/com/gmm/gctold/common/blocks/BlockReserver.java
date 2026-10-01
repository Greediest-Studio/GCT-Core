package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

  public class BlockReserver extends Block {
  public static final Block block = new BlockReserver();

  public BlockReserver() {
    super(Material.ROCK);
    setTranslationKey("reserver");
    setSoundType(SoundType.GROUND);
    setHardness(1.0F);
    setResistance(10.0F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }
}
