
package com.gmm.gctold.common.events;

import com.gmm.gctold.common.items.ItemCreepyWitherstormDoll;
import com.gmm.gctold.common.world.dimension.WorldTheVoid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public final class CreepyWitherstormDollClick
{
    private static final ResourceLocation WITHER_STORM = new ResourceLocation("ageofminecraft", "witherbosscommandblockhelpful");

    private CreepyWitherstormDollClick() {
    }
    
    public static void run(Entity entity, World world, int x, int y, int z) {
        if (entity.dimension == WorldTheVoid.DIMID) {
            if (entity instanceof EntityPlayer) {
                ((EntityPlayer) entity).sendMessage(new TextComponentString("小心，它来了！"));
            }
            if (!world.isRemote) {
                Entity witherStorm = EntityList.createEntityByIDFromName(WITHER_STORM, world);
                if (witherStorm != null) {
                    witherStorm.setLocationAndAngles(x + 0.5D, y + 5.0D, z + 0.5D, entity.rotationYaw, 0.0F);
                    world.spawnEntity(witherStorm);
                }
            }
            if (entity instanceof EntityPlayer) {
                ((EntityPlayer)entity).inventory.clearMatchingItems(new ItemStack(ItemCreepyWitherstormDoll.block, 1).getItem(), -1, 1, (NBTTagCompound)null);
            }
        }
        else {
            if (entity instanceof EntityPlayer) {
                ((EntityPlayer) entity).sendMessage(new TextComponentString("恐怖的风暴凋灵玩偶：我貌似不能在这个维度施展能力……"));
            }
        }
    }
}


