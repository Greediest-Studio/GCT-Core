package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import com.google.common.base.Predicate;
import java.util.Random;
import com.gmm.gctold.common.world.biome.BiomeManaForest;
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
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;

  public class BlockManaStone extends Block {
  public static final Block block = new BlockManaStone();public void generateWorld(Random random, int chunkX, int chunkZ, World world, int dimID, IChunkGenerator cg, IChunkProvider cp) {
    boolean dimensionCriteria = false;
    if (dimID == 0)
      dimensionCriteria = true;
    if (!dimensionCriteria)
      return;
    boolean biomeCriteria = false;
    Biome biome = world.getBiome(new BlockPos(chunkX, 128, chunkZ));
    if (biome == BiomeManaForest.biome)
      biomeCriteria = true;
    if (!biomeCriteria)
      return;
    for (int i = 0; i < 32; i++) {
      int x = chunkX + random.nextInt(16);
      int y = random.nextInt(64) + 0;
      int z = chunkZ + random.nextInt(16);
      (new WorldGenMinable(block.getDefaultState(), 32, new Predicate<IBlockState>() {
            public boolean apply(IBlockState blockAt) {
              boolean blockCriteria = false;
              if (blockAt.getBlock() == Blocks.STONE.getDefaultState().getBlock())
                blockCriteria = true;
              return blockCriteria;
            }
          })).generate(world, random, new BlockPos(x, y, z));
    }
  }

  public BlockManaStone() {
    super(Material.ROCK);
    setTranslationKey("mana_stone");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 0);
    setHardness(1.5F);
    setResistance(6.0F);
    setLightLevel(0.33333334F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }

  public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
    drops.add(new ItemStack(BlockManaCobbleStone.block, 1));
  }
}
