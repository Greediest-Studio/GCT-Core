package com.gmm.gctold.common.events;

import com.gmm.gctold.common.blocks.BlockBesideVoidPortal1;
import com.gmm.gctold.common.blocks.BlockBesideVoidPortal2;
import com.gmm.gctold.common.blocks.BlockBesideVoidPortal3;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class BesideVoidPortalDestroy2 {
  private static final ResourceLocation CULTIST_CLERIC = new ResourceLocation("thaumcraft", "cultistcleric");

  private BesideVoidPortalDestroy2() {
  }

  public static void run(World world, int x, int y, int z) {
    if (world.getBlockState(new BlockPos(x + 0, y + 3, z + 0)).getBlock() == BlockBesideVoidPortal1.block
      .getDefaultState().getBlock() || world
      .getBlockState(new BlockPos(x + 0, y + 2, z + 0)).getBlock() == BlockBesideVoidPortal2.block
      .getDefaultState().getBlock() || world
      .getBlockState(new BlockPos(x + 0, y + 1, z + 0)).getBlock() == BlockBesideVoidPortal3.block
      .getDefaultState().getBlock()) {
      world.setBlockState(new BlockPos(x, y + 1, z), Blocks.AIR.getDefaultState(), 3);
      world.setBlockState(new BlockPos(x, y + 2, z), Blocks.AIR.getDefaultState(), 3);
      world.setBlockState(new BlockPos(x, y + 3, z), Blocks.AIR.getDefaultState(), 3);
      if (!world.isRemote)
        world.createExplosion(null, x, y, z, 4.0F, true);
      if (!world.isRemote) {
        for (int i = 0; i < 4; i++) {
          spawnEntity(world, CULTIST_CLERIC, x + 0.5D, y + 1.0D, z + 0.5D);
        }
      }
    }
  }

  private static void spawnEntity(World world, ResourceLocation id, double x, double y, double z) {
    Entity entity = EntityList.createEntityByIDFromName(id, world);
    if (entity != null) {
      entity.setLocationAndAngles(x, y, z, world.rand.nextFloat() * 360.0F, 0.0F);
      world.spawnEntity(entity);
    }
  }
}

