package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class BlockYogsothothiumOre extends Block {
  public static final Block block = new BlockYogsothothiumOre();

  public BlockYogsothothiumOre() {
    super(Material.ROCK);
    setTranslationKey("yogsothothium_ore");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 11);
    setHardness(30.0F);
    setResistance(4000.0F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }
}
