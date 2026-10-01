package com.gmm.gctold.common.events;

import com.gmm.gctold.common.blocks.BlockEarthboundCore;
import com.gmm.gctold.common.blocks.BlockEarthboundReceiver;
import com.gmm.gctold.common.compat.NaturesAuraCompat;
import com.gmm.gctold.misc.registry.GctAllItems;
import com.gmm.gctold.common.items.ItemEarthOrb;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class EarthboundAltarClick {
  private static final int[][] RECEIVER_OFFSETS = {
      {4, -1, 0},
      {-4, -1, 0},
      {0, -1, 4},
      {0, -1, -4}
  };
  private static final int[][] CORE_OFFSETS = {
      {3, -1, 3},
      {-3, -1, 3},
      {3, -1, -3},
      {-3, -1, -3}
  };
  private static final int[][] GRASS_OFFSETS = {
      {-1, -1, -1},
      {-1, -1, 1},
      {1, -1, 1},
      {1, -1, -1},
      {1, -1, 0},
      {-1, -1, 0},
      {0, -1, 1},
      {0, -1, -1}
  };

  private EarthboundAltarClick() {
  }

  public static void run(Entity entity, World world, int x, int y, int z) {
    BlockPos altarPos = new BlockPos(x, y, z);
    if (!hasValidStructure(world, altarPos)) {
      playSound(world, x, y, z, "block.anvil.land");
      return;
    }

    if (!(entity instanceof EntityLivingBase)) {
      return;
    }

    EntityLivingBase living = (EntityLivingBase) entity;
    if (living.getHeldItemMainhand().getItem() != GctAllItems.HOLYSTEEL_INGOT) {
      return;
    }

    if (entity instanceof EntityPlayer) {
      ((EntityPlayer) entity).inventory.clearMatchingItems(GctAllItems.HOLYSTEEL_INGOT, -1, 1, null);
    }
    living.swingArm(EnumHand.MAIN_HAND);

    for (int i = 0; i < 10; i++) {
      world.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, x, y, z, 0.0D, 1.0D, 0.0D);
    }

    if (!world.isRemote) {
      ItemStack result = world.rand.nextDouble() < 0.5D
          ? new ItemStack(GctAllItems.EARTH_INGOT, 1)
          : new ItemStack(Blocks.DIRT, 1, 0);
      EntityItem item = new EntityItem(world, x, y + 1, z, result);
      item.setPickupDelay(10);
      world.spawnEntity(item);
    }

    Random random = new Random();
    for (int[] offset : RECEIVER_OFFSETS) {
      damageOrb(world, altarPos.add(offset[0], offset[1], offset[2]), random);
    }

    NaturesAuraCompat.drainAura(world, altarPos, 100000);
    playSound(world, x, y, z, "block.metal.break");
  }

  private static boolean hasValidStructure(World world, BlockPos altarPos) {
    return allBlocks(world, altarPos, RECEIVER_OFFSETS, BlockEarthboundReceiver.block)
        && allBlocks(world, altarPos, CORE_OFFSETS, BlockEarthboundCore.block)
        && allBlocks(world, altarPos, GRASS_OFFSETS, Blocks.GRASS)
        && allReceiverSlotsContain(world, altarPos, ItemEarthOrb.block);
  }

  private static boolean allBlocks(World world, BlockPos origin, int[][] offsets, Block block) {
    for (int[] offset : offsets) {
      if (world.getBlockState(origin.add(offset[0], offset[1], offset[2])).getBlock() != block) {
        return false;
      }
    }
    return true;
  }

  private static boolean allReceiverSlotsContain(World world, BlockPos origin, Item item) {
    for (int[] offset : RECEIVER_OFFSETS) {
      if (getStack(world, origin.add(offset[0], offset[1], offset[2]), 0).getItem() != item) {
        return false;
      }
    }
    return true;
  }

  private static ItemStack getStack(World world, BlockPos pos, int slot) {
    TileEntity inv = world.getTileEntity(pos);
    return inv instanceof TileEntityLockableLoot ? ((TileEntityLockableLoot) inv).getStackInSlot(slot) : ItemStack.EMPTY;
  }

  private static void damageOrb(World world, BlockPos pos, Random random) {
    TileEntity inv = world.getTileEntity(pos);
    if (!(inv instanceof TileEntityLockableLoot)) {
      return;
    }

    ItemStack stack = ((TileEntityLockableLoot) inv).getStackInSlot(0);
    if (!stack.isEmpty() && stack.attemptDamageItem(1, random, null)) {
      stack.shrink(1);
      stack.setItemDamage(0);
    }
  }

  private static void playSound(World world, int x, int y, int z, String soundId) {
    SoundEvent sound = SoundEvent.REGISTRY.getObject(new ResourceLocation(soundId));
    if (sound != null) {
      world.playSound((EntityPlayer) null, x, y, z, sound, SoundCategory.NEUTRAL, 1.0F, 1.0F);
    }
  }
}
