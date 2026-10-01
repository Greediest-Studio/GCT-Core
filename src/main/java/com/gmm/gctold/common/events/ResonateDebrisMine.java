package com.gmm.gctold.common.events;

import com.gmm.gctold.misc.registry.GctAllItems;

import com.gmm.gctold.common.blocks.BlockResonateDebris;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public final class ResonateDebrisMine {
  private ResonateDebrisMine() {
  }

  public static void run(Entity entity, World world, int x, int y, int z) {
    if (((entity instanceof EntityLivingBase) ? ((EntityLivingBase)entity).getHeldItemMainhand() : ItemStack.EMPTY).getItem()
      .canHarvestBlock(BlockResonateDebris.block.getDefaultState())) {
      if (!world.isRemote)
        for (int i = 0; i < 2; i++) {
        EntityItem entityToSpawn = new EntityItem(world, x, y, z, new ItemStack(GctAllItems.RESONATED_SCRAP, 1));
        entityToSpawn.setPickupDelay(10);
        world.spawnEntity((Entity)entityToSpawn);
      }
      if (!world.isRemote)
        world.spawnEntity((Entity)new EntityXPOrb(world, x, y, z, 30 + world.rand.nextInt(15)));
    }
  }
}

