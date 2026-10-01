package com.gmm.gctold.common.events;

import com.gmm.gctold.common.entity.EntityAncientShoggoth;
import com.gmm.gctold.common.world.dimension.WorldDarkerRealm;
import com.gmm.gctold.misc.registry.GctAllItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public final class SanityAltarNeighbourBlockChanges {
  private SanityAltarNeighbourBlockChanges() {
  }

  public static void run(World world, int x, int y, int z) {
    BlockPos altarPos = new BlockPos(x, y, z);
    if (getStack(world, altarPos, 0).getItem() != GctAllItems.ANCIENT_MUD) {
      tell(world, altarPos, "祭坛没有被远古污泥填充！");
      return;
    }

    if (world.provider.getDimension() != WorldDarkerRealm.DIMID) {
      tell(world, altarPos, "你应该在深暗领域召唤远古修格斯！");
      return;
    }

    if (world.getBlockState(altarPos.up()).getBlock() != Blocks.OBSIDIAN) {
      tell(world, altarPos, "祭坛上方应该放置一块黑曜石！");
      return;
    }

    if (y > 90) {
      tell(world, altarPos, "祭坛的高度应该在90层以下！");
      return;
    }

    clearSummonArea(world, altarPos);
    TileEntity inv = world.getTileEntity(altarPos);
    if (inv instanceof TileEntityLockableLoot) {
      ((TileEntityLockableLoot)inv).decrStackSize(0, 1);
    }

    if (!world.isRemote) {
      EntityAncientShoggoth.AncientShoggothEntity shoggoth = new EntityAncientShoggoth.AncientShoggothEntity(world);
      shoggoth.setLocationAndAngles(x, y + 5, z, world.rand.nextFloat() * 360.0F, 0.0F);
      world.spawnEntity(shoggoth);
    }
    tell(world, altarPos, "你打扰了远古的野兽……");
  }

  private static ItemStack getStack(World world, BlockPos pos, int slot) {
    TileEntity inv = world.getTileEntity(pos);
    return inv instanceof TileEntityLockableLoot ? ((TileEntityLockableLoot)inv).getStackInSlot(slot) : ItemStack.EMPTY;
  }

  private static void clearSummonArea(World world, BlockPos altarPos) {
    if (world.isRemote) {
      return;
    }
    for (int dx = -15; dx <= 15; dx++) {
      for (int dy = 1; dy <= 30; dy++) {
        for (int dz = -15; dz <= 15; dz++) {
          BlockPos pos = altarPos.add(dx, dy, dz);
          if (world.getBlockState(pos).getBlock() != Blocks.AIR) {
            world.setBlockToAir(pos);
          }
        }
      }
    }
  }

  private static void tell(World world, BlockPos pos, String message) {
    if (world.isRemote) {
      return;
    }
    TextComponentString component = new TextComponentString(message);
    AxisAlignedBB area = new AxisAlignedBB(pos).grow(5.0D);
    for (EntityPlayer player : world.getEntitiesWithinAABB(EntityPlayer.class, area,
        player -> player.getDistanceSq(pos) <= 25.0D)) {
      player.sendMessage(component);
    }
  }
}
