package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

  public class BlockAstralPortalCore extends Block {
  public static final Block block = new BlockAstralPortalCore();

  public BlockAstralPortalCore() {
    super(Material.ROCK);
    setTranslationKey("astral_portal_core");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 0);
    setHardness(10.0F);
    setResistance(10.0F);
    setLightLevel(1.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }
}
