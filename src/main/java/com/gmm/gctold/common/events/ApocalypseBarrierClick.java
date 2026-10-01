package com.gmm.gctold.common.events;

import com.gmm.gctold.misc.registry.GctAllItems;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class ApocalypseBarrierClick {
  private ApocalypseBarrierClick() {
  }

  public static void run(Entity entity, World world, int x, int y, int z) {
    if (((entity instanceof EntityLivingBase) ? ((EntityLivingBase)entity).getHeldItemMainhand() : ItemStack.EMPTY)
      .getItem() == (new ItemStack(GctAllItems.APOCALYPSE_RUIN, 1)).getItem())
      world.setBlockToAir(new BlockPos(x, y, z));
  }
}

