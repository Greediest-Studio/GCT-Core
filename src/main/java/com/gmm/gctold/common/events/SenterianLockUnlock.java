
package com.gmm.gctold.common.events;

import com.gmm.gctold.misc.registry.GctAllItems;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import net.minecraft.entity.Entity;
public final class SenterianLockUnlock
{
    private SenterianLockUnlock() {
    }
    
    public static void run(Entity entity, World world, int x, int y, int z) {
        if (((entity instanceof EntityLivingBase) ? ((EntityLivingBase)entity).getHeldItemMainhand() : ItemStack.EMPTY).getItem() == new ItemStack(GctAllItems.SENTERIAN_KEY, 1).getItem()) {
            if (entity instanceof EntityPlayer) {
                ((EntityPlayer)entity).inventory.clearMatchingItems(new ItemStack(GctAllItems.SENTERIAN_KEY, 1).getItem(), -1, 1, (NBTTagCompound)null);
            }
            world.setBlockToAir(new BlockPos(x, y, z));
            world.playSound((EntityPlayer)null, (double)x, (double)y, (double)z, (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation("block.fence_gate.open")), SoundCategory.NEUTRAL, 1.0f, 1.0f);
        }
    }
}


