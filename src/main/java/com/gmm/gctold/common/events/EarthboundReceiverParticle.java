package com.gmm.gctold.common.events;

import com.gmm.gctold.common.items.ItemEarthOrb;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class EarthboundReceiverParticle {
  private EarthboundReceiverParticle() {
  }

  public static boolean run(World world, int x, int y, int z) {
    return getItemStack(world, new BlockPos(x, y, z), 0).getItem() == ItemEarthOrb.block;
  }

  private static ItemStack getItemStack(World world, BlockPos pos, int slot) {
    TileEntity tile = world.getTileEntity(pos);
    if (tile instanceof TileEntityLockableLoot) {
      return ((TileEntityLockableLoot) tile).getStackInSlot(slot);
    }
    return ItemStack.EMPTY;
  }
}

