package com.gmm.gctold.common.events;

import com.gmm.gctold.common.blocks.BlockBesideVoidPortal2;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class BesideVoidPortalUp {
  private BesideVoidPortalUp() {
  }

  public static void run(World world, int x, int y, int z) {
    if (world.getBlockState(new BlockPos(x, y - 1, z)).getBlock() != BlockBesideVoidPortal2.block.getDefaultState()
      .getBlock())
      world.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState(), 3);
  }
}

