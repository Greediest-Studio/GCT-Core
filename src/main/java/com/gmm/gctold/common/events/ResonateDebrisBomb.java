package com.gmm.gctold.common.events;

import com.gmm.gctold.common.blocks.BlockResonateDebrisCracked;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class ResonateDebrisBomb {
  private ResonateDebrisBomb() {
  }

  public static void run(World world, int x, int y, int z) {
    world.setBlockState(new BlockPos(x, y, z), BlockResonateDebrisCracked.block.getDefaultState(), 3);
  }
}

