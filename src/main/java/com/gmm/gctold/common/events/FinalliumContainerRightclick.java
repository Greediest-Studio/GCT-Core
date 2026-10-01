
package com.gmm.gctold.common.events;

import com.gmm.gctold.common.blocks.BlockFinalliumContainerActive;
import com.gmm.gctold.misc.registry.GctAllItems;
import net.minecraft.util.math.BlockPos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraft.entity.Entity;
public final class FinalliumContainerRightclick
{
    private FinalliumContainerRightclick() {
    }
    
    public static void run(Entity entity, World world, int x, int y, int z) {
        if (((entity instanceof EntityLivingBase) ? ((EntityLivingBase)entity).getHeldItemMainhand() : ItemStack.EMPTY).getItem() == new ItemStack(GctAllItems.ORDERED_CORE, 1).getItem()) {
            if (entity instanceof EntityLivingBase) {
                ((EntityLivingBase)entity).swingArm(EnumHand.MAIN_HAND);
            }
            if (entity instanceof EntityPlayer) {
                ((EntityPlayer)entity).inventory.clearMatchingItems(new ItemStack(GctAllItems.ORDERED_CORE, 1).getItem(), -1, 1, (NBTTagCompound)null);
            }
            world.setBlockState(new BlockPos(x, y, z), BlockFinalliumContainerActive.block.getDefaultState(), 3);
        }
    }
}


