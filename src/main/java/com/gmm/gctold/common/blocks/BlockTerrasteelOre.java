package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import com.google.common.base.Predicate;
import java.util.Random;
import com.gmm.gctold.common.world.dimension.WorldAlfheim;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;

  public class BlockTerrasteelOre extends Block {
  public static final Block block = new BlockTerrasteelOre();public void generateWorld(Random random, int chunkX, int chunkZ, World world, int dimID, IChunkGenerator cg, IChunkProvider cp) {
    boolean dimensionCriteria = false;
    if (dimID == WorldAlfheim.DIMID)
      dimensionCriteria = true;
    if (!dimensionCriteria)
      return;
    for (int i = 0; i < 2; i++) {
      int x = chunkX + random.nextInt(16);
      int y = random.nextInt(64) + 0;
      int z = chunkZ + random.nextInt(16);
      (new WorldGenMinable(block.getDefaultState(), 3, new Predicate<IBlockState>() {
            public boolean apply(IBlockState blockAt) {
              boolean blockCriteria = false;
              if (blockAt.getBlock() == BlockAlfStone.block.getDefaultState().getBlock())
                blockCriteria = true;
              return blockCriteria;
            }
          })).generate(world, random, new BlockPos(x, y, z));
    }
  }

  public BlockTerrasteelOre() {
    super(Material.ROCK);
    setTranslationKey("terrasteel_ore");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 9);
    setHardness(5.0F);
    setResistance(6.0F);
    setLightLevel(0.33333334F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }
}
