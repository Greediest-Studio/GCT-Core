package com.gmm.gctold.common.blocks;

import com.gmm.gctold.misc.GctAllCreativeTab;

import com.google.common.base.Predicate;
import java.util.Random;
import com.gmm.gctold.common.events.ResonateDebrisBomb;
import com.gmm.gctold.common.events.ResonateDebrisMine;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;

  public class BlockResonateDebris extends Block {
  public static final Block block = new BlockResonateDebris();public void generateWorld(Random random, int chunkX, int chunkZ, World world, int dimID, IChunkGenerator cg, IChunkProvider cp) {
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

  public BlockResonateDebris() {
    super(Material.ROCK);
    setTranslationKey("resonate_debris");
    setSoundType(SoundType.STONE);
    setHarvestLevel("pickaxe", 12);
    setHardness(100.0F);
    setResistance(30.0F);
    setLightLevel(0.0F);
    setLightOpacity(255);
    setCreativeTab(GctAllCreativeTab.TAB);
  }

  public int quantityDropped(Random random) {
    return 0;
  }

  public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer entity, boolean willHarvest) {
    boolean retval = super.removedByPlayer(state, world, pos, entity, willHarvest);
    int x = pos.getX();
    int y = pos.getY();
    int z = pos.getZ();
      ResonateDebrisMine.run(entity, world, x, y, z);
    return retval;
  }

  public void onExplosionDestroy(World world, BlockPos pos, Explosion e) {
    super.onExplosionDestroy(world, pos, e);
    int x = pos.getX();
    int y = pos.getY();
    int z = pos.getZ();
      ResonateDebrisBomb.run(world, x, y, z);
  }
}
