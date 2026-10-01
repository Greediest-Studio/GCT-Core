package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

  public class BlockReversedAlfCobbleStone extends Block {
  public static final Block block = new BlockReversedAlfCobbleStone();

  public BlockReversedAlfCobbleStone() {
    super(Material.ROCK);
    setTranslationKey("reversed_alf_cobble_stone");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 0);
    setHardness(2.0F);
    setResistance(6.0F);
    setLightLevel(0.33333334F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }
}
