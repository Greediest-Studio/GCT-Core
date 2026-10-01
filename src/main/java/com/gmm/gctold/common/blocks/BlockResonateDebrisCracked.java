package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.registry.GctAllItems;

import com.gmm.gctold.misc.GctAllCreativeTab;

import com.google.common.base.Predicate;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;

  public class BlockResonateDebrisCracked extends Block {
  public static final Block block = new BlockResonateDebrisCracked();public void generateWorld(Random random, int chunkX, int chunkZ, World world, int dimID, IChunkGenerator cg, IChunkProvider cp) {
    boolean dimensionCriteria = false;
    if (dimID == 1)
      dimensionCriteria = true;
    if (!dimensionCriteria)
      return;
    for (int i = 0; i < 2; i++) {
      int x = chunkX + random.nextInt(16);
      int y = random.nextInt(80) + 0;
      int z = chunkZ + random.nextInt(16);
      (new WorldGenMinable(block.getDefaultState(), 1, new Predicate<IBlockState>() {
            public boolean apply(IBlockState blockAt) {
              boolean blockCriteria = false;
              if (blockAt.getBlock() == Blocks.END_STONE.getDefaultState().getBlock())
                blockCriteria = true;
              return blockCriteria;
            }
          })).generate(world, random, new BlockPos(x, y, z));
    }
  }

  public BlockResonateDebrisCracked() {
    super(Material.ROCK);
    setTranslationKey("resonate_debris_cracked");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 9);
    setHardness(50.0F);
    setResistance(2000.0F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }

  public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
    drops.add(new ItemStack(GctAllItems.RESONATED_SCRAP, 1));
  }
}
