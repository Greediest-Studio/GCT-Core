
package com.gmm.gctold.common.events;

import com.gmm.gctold.common.world.dimension.WorldTheNowhere;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public final class RNGRelinquisherClick
{
    private RNGRelinquisherClick() {
    }
    
    public static void run(Entity entity, World world, int x, int y, int z) {
        if (entity.dimension == 1 && x == 0 && z == 0) {
            if (!entity.world.isRemote && !entity.isRiding() && !entity.isBeingRidden() && entity instanceof EntityPlayerMP) {
                TeleporterDirect.transferToSpawn((EntityPlayerMP) entity, WorldTheNowhere.DIMID);
            }
        }
        else if (entity.dimension == WorldTheNowhere.DIMID) {
            if (!entity.world.isRemote && !entity.isRiding() && !entity.isBeingRidden() && entity instanceof EntityPlayerMP) {
                TeleporterDirect.transferToSpawn((EntityPlayerMP) entity, 1);
            }
        }
        else if (entity.dimension != 1 && entity.dimension != WorldTheNowhere.DIMID) {
            if (entity instanceof EntityPlayerMP) {
                ((EntityPlayerMP) entity).sendMessage(new TextComponentString("貌似不能在这个维度使用这个物品……"));
            }
        }
        else {
            if (entity instanceof EntityPlayerMP) {
                ((EntityPlayerMP) entity).sendMessage(new TextComponentString("或许我需要抵达末路之地的原点？"));
            }
        }
    }
}


