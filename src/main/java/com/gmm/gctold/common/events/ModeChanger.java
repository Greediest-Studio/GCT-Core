
package com.gmm.gctold.common.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.GameType;
import net.minecraft.world.World;

public final class ModeChanger
{
    private ModeChanger() {
    }
    
    public static void run(Entity entity, World world, int x, int y, int z) {
        if (entity instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) entity;
            player.setGameType(player.capabilities.isCreativeMode ? GameType.SURVIVAL : GameType.CREATIVE);
        }
    }
}


