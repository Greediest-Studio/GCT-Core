package com.gmm.gctold.common.events;

import com.gmm.gctold.common.blocks.BlockArcaneVisReceiver;
import com.gmm.gctold.common.blocks.BlockPrimordialPortalHolder2;
import com.gmm.gctold.common.blocks.BlockPrimordialStone;
import com.gmm.gctold.common.blocks.BlockPrimordialVisReceiver;
import com.gmm.gctold.common.blocks.BlockRuinHolder;
import com.gmm.gctold.common.blocks.BlockVoidSeedContainer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class BesideVoidPortalDestroy {
  private BesideVoidPortalDestroy() {
  }

  public static void run(World world, int x, int y, int z) {
    if (world.getBlockState(new BlockPos(x + 0, y - 1, z + 0)).getBlock() != BlockPrimordialVisReceiver.block
      .getDefaultState().getBlock() || world
      .getBlockState(new BlockPos(x + 1, y - 1, z + 0)).getBlock() != BlockPrimordialStone.block
      .getDefaultState().getBlock() || world
      .getBlockState(new BlockPos(x + 2, y - 1, z + 0)).getBlock() != BlockPrimordialStone.block
      .getDefaultState().getBlock() || world
      .getBlockState(new BlockPos(x + 3, y - 1, z + 0))
      .getBlock() != BlockArcaneVisReceiver.block.getDefaultState().getBlock() || world
      .getBlockState(new BlockPos(x - 1, y - 1, z + 0)).getBlock() != BlockPrimordialStone.block
      .getDefaultState().getBlock() || world
      .getBlockState(new BlockPos(x - 2, y - 1, z + 0)).getBlock() != BlockPrimordialStone.block
      .getDefaultState().getBlock() || world
      .getBlockState(new BlockPos(x - 3, y - 1, z + 0))
      .getBlock() != BlockArcaneVisReceiver.block.getDefaultState().getBlock() || world
      .getBlockState(new BlockPos(x + 0, y - 1, z + 1))
      .getBlock() != BlockPrimordialStone.block.getDefaultState().getBlock() || world
      .getBlockState(new BlockPos(x + 0, y - 1, z + 2)).getBlock() != BlockPrimordialStone.block
      .getDefaultState().getBlock() || world
      .getBlockState(new BlockPos(x + 0, y - 1, z + 3))
      .getBlock() != BlockArcaneVisReceiver.block.getDefaultState().getBlock() || world
      .getBlockState(new BlockPos(x + 0, y - 1, z - 1)).getBlock() != BlockPrimordialStone.block
      .getDefaultState().getBlock() || world
      .getBlockState(new BlockPos(x + 0, y - 1, z - 2))
      .getBlock() != BlockPrimordialStone.block.getDefaultState().getBlock() || world
      .getBlockState(new BlockPos(x + 0, y - 1, z - 3))
      .getBlock() != BlockArcaneVisReceiver.block.getDefaultState().getBlock() || world
      .getBlockState(new BlockPos(x + 3, y + 0, z + 0))
      .getBlock() != BlockRuinHolder.block.getDefaultState().getBlock() || world
      .getBlockState(new BlockPos(x - 3, y + 0, z + 0))
      .getBlock() != BlockRuinHolder.block.getDefaultState().getBlock() || world
      .getBlockState(new BlockPos(x + 0, y + 0, z + 3))
      .getBlock() != BlockRuinHolder.block.getDefaultState().getBlock() || world
      .getBlockState(new BlockPos(x + 0, y + 0, z - 3)).getBlock() != BlockRuinHolder.block
      .getDefaultState().getBlock() || world
      .getBlockState(new BlockPos(x + 3, y + 1, z + 0))
      .getBlock() != BlockVoidSeedContainer.block.getDefaultState().getBlock() || world
      .getBlockState(new BlockPos(x - 3, y + 1, z + 0))
      .getBlock() != BlockVoidSeedContainer.block.getDefaultState().getBlock() || world
      .getBlockState(new BlockPos(x + 0, y + 1, z + 3))
      .getBlock() != BlockVoidSeedContainer.block.getDefaultState().getBlock() || world
      .getBlockState(new BlockPos(x + 0, y + 1, z - 3))
      .getBlock() != BlockVoidSeedContainer.block.getDefaultState().getBlock() || world
      .getBlockState(new BlockPos(x + 1, y - 1, z + 1))
      .getBlock() != BlockPrimordialStone.block.getDefaultState().getBlock() || world
      .getBlockState(new BlockPos(x + 1, y - 1, z - 1))
      .getBlock() != BlockPrimordialStone.block.getDefaultState().getBlock() || world
      .getBlockState(new BlockPos(x - 1, y - 1, z + 1))
      .getBlock() != BlockPrimordialStone.block.getDefaultState().getBlock() || world
      .getBlockState(new BlockPos(x - 1, y - 1, z - 1)).getBlock() != BlockPrimordialStone.block
      .getDefaultState().getBlock() || world
      .getBlockState(new BlockPos(x + 0, y + 4, z + 0))
      .getBlock() != BlockPrimordialPortalHolder2.block.getDefaultState().getBlock()) {
      world.setBlockState(new BlockPos(x, y + 1, z), Blocks.AIR.getDefaultState(), 3);
      world.setBlockState(new BlockPos(x, y + 2, z), Blocks.AIR.getDefaultState(), 3);
      world.setBlockState(new BlockPos(x, y + 3, z), Blocks.AIR.getDefaultState(), 3);
    }
  }
}

