package com.gmm.gctold.common.events;

import com.gmm.gctold.misc.registry.GctAllItems;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public final class SanityRenderDisplayOverlayIngame {
  private SanityRenderDisplayOverlayIngame() {
  }

  public static boolean run(Entity entity) {
    if (entity instanceof EntityPlayer && ((EntityPlayer)entity).inventory
      .hasItemStack(new ItemStack(GctAllItems.SANITY_OBSERVER, 1)))
      return true;
    return false;
  }
}

