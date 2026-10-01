package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

  public class BlockPrimordialStoneStairs extends BlockStairs {
  public static final Block block = new BlockPrimordialStoneStairs();

  public BlockPrimordialStoneStairs() {
    super((new Block(Material.ROCK)).getDefaultState());
    setTranslationKey("primordial_stone_stairs");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 4);
    setHardness(2.0F);
    setResistance(10.0F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }
}
