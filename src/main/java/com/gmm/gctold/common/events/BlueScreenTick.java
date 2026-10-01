package com.gmm.gctold.common.events;

import com.gmm.gctold.common.potions.PotionBlueScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public final class BlueScreenTick {
  private BlueScreenTick() {
  }

  public static boolean run(Entity entity) {
    return entity instanceof EntityLivingBase && ((EntityLivingBase) entity).isPotionActive(PotionBlueScreen.potion);
  }
}

