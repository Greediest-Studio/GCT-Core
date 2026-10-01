
package com.gmm.gctold.common.events;

import com.gmm.gctold.common.world.dimension.WorldTheVoid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public final class CreepyWitherDollClick
{
    private CreepyWitherDollClick() {
    }
    
    public static void run(Entity entity, World world, int x, int y, int z) {
        if (entity.dimension == 1 && x == 0 && z == 0) {
            if (!entity.world.isRemote && !entity.isRiding() && !entity.isBeingRidden() && entity instanceof EntityPlayerMP) {
                TeleporterDirect.transferToSpawn((EntityPlayerMP) entity, WorldTheVoid.DIMID);
            }
        }
        else if (entity.dimension == WorldTheVoid.DIMID) {
            if (!entity.world.isRemote && !entity.isRiding() && !entity.isBeingRidden() && entity instanceof EntityPlayerMP) {
                TeleporterDirect.transferToSpawn((EntityPlayerMP) entity, 1);
            }
        }
        else if (entity.dimension != 1 && entity.dimension != WorldTheVoid.DIMID) {
            if (entity instanceof EntityPlayerMP) {
                ((EntityPlayerMP) entity).sendMessage(new TextComponentString("可怕的凋灵玩偶：我貌似不能在这个维度施展能力……"));
            }
        }
        else {
            if (entity instanceof EntityPlayerMP) {
                ((EntityPlayerMP) entity).sendMessage(new TextComponentString("可怕的凋灵玩偶：只有来到了末路之地的原点，我才能施展能力！"));
            }
        }
    }
}


