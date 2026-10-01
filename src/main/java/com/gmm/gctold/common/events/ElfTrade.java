package com.gmm.gctold.common.events;

import com.gmm.gctold.misc.registry.GctAllItems;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

public final class ElfTrade {
  private ElfTrade() {
  }

  public static void run(Entity entity, World world, int x, int y, int z) {
    if (((entity instanceof EntityLivingBase) ? ((EntityLivingBase)entity).getHeldItemMainhand() : ItemStack.EMPTY)
      .getItem() == (new ItemStack(GctAllItems.ELF_PASSES, 1)).getItem()) {
      if (entity instanceof EntityPlayer)
        ((EntityPlayer)entity).inventory.clearMatchingItems((new ItemStack(GctAllItems.ELF_PASSES, 1)).getItem(), -1, 1, null);
      messageNearbyPlayers(world, x, y, z, "<精灵>这东西真不错！");
      if (entity instanceof EntityPlayer) {
        ItemStack _setstack = new ItemStack(GctAllItems.NATURALLINE_SCRAP, 1);
        _setstack.setCount(1);
        ItemHandlerHelper.giveItemToPlayer((EntityPlayer)entity, _setstack);
      }
    } else {
      messageNearbyPlayers(world, x, y, z, "<精灵>你似乎不能不劳而获呢。");
    }
  }

  private static void messageNearbyPlayers(World world, int x, int y, int z, String message) {
    for (EntityPlayer player : world.playerEntities) {
      if (player.getDistanceSq(x + 0.5D, y + 0.5D, z + 0.5D) <= 32.0D * 32.0D) {
        player.sendMessage(new TextComponentString(message));
      }
    }
  }
}


