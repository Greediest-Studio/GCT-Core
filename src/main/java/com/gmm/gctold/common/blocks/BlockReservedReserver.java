package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

  public class BlockReservedReserver extends Block {
  public static final Block block = new BlockReservedReserver();

  public BlockReservedReserver() {
    super(Material.ROCK);
    setTranslationKey("reserved_reserver");
    setSoundType(SoundType.STONE);
    setHardness(1.0F);
    setResistance(10.0F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
    setBlockUnbreakable();
  }
}
