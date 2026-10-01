package com.gmm.gctold.common.events;

import com.gmm.gctold.common.world.dimension.WorldBesideVoid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

public final class BesideVoidTeleport {
  private BesideVoidTeleport() {
  }

  public static void run(Entity entity, World world) {
    Entity _ent = entity;
    if (!_ent.world.isRemote && !_ent.isRiding() && !_ent.isBeingRidden() && _ent instanceof EntityPlayerMP) {
      int dimensionID = (_ent.dimension == WorldBesideVoid.DIMID) ? 0 : WorldBesideVoid.DIMID;
      EntityPlayerMP _player = (EntityPlayerMP)_ent;
      TeleporterDirect.transferToSpawn(_player, dimensionID);
    }
  }
}

