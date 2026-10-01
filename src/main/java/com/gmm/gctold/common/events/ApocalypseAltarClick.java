package com.gmm.gctold.common.events;

import com.gmm.gctold.misc.registry.GctAllItems;

import com.gmm.gctold.common.entity.EntityApocalypseCube;
import com.gmm.gctold.common.entity.EntityApocalypseKnight;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public final class ApocalypseAltarClick {
  private ApocalypseAltarClick() {
  }

  public static void run(Entity entity, World world, int x, int y, int z) {
    if (((entity instanceof EntityLivingBase) ? ((EntityLivingBase)entity).getHeldItemMainhand() : ItemStack.EMPTY)
      .getItem() == (new ItemStack(GctAllItems.APOCALYPSE_RUIN, 1)).getItem()) {
      if (entity instanceof EntityPlayer)
        ((EntityPlayer)entity).inventory.clearMatchingItems((new ItemStack(GctAllItems.APOCALYPSE_RUIN, 1)).getItem(), -1, 1, null);
      if (!world.isRemote) {
        EntityApocalypseCube.ApocalypseCubeEntity cube = new EntityApocalypseCube.ApocalypseCubeEntity(world);
        cube.setLocationAndAngles(x + 0.5D, y + 4.0D, z + 0.5D, entity.rotationYaw, 0.0F);
        world.spawnEntity(cube);

        EntityApocalypseKnight.ApocalypseKnightEntity knight = new EntityApocalypseKnight.ApocalypseKnightEntity(world);
        knight.setLocationAndAngles(x + 0.5D, y + 4.0D, z + 0.5D, entity.rotationYaw, 0.0F);
        world.spawnEntity(knight);
        knight.startRiding(cube, true);
      }
    }
  }
}

