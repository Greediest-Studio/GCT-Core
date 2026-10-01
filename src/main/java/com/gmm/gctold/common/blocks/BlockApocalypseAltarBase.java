package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

  public class BlockApocalypseAltarBase extends Block {
  public static final Block block = new BlockApocalypseAltarBase();

  public BlockApocalypseAltarBase() {
    super(Material.ROCK);
    setTranslationKey("apocalypse_altar_base");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 25);
    setHardness(200.0F);
    setResistance(10000.0F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }

  public int quantityDropped(Random random) {
    return 0;
  }
}
