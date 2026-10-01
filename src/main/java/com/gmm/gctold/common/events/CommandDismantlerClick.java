
package com.gmm.gctold.common.events;

import net.minecraft.entity.player.EntityPlayerMP;
import java.util.Random;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.Entity;

public final class CommandDismantlerClick
{
    private CommandDismantlerClick() {
    }
    
    public static void run(Entity entity, ItemStack itemstack, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        Block block = world.getBlockState(pos).getBlock();
        if (block == Blocks.COMMAND_BLOCK || block == Blocks.REPEATING_COMMAND_BLOCK
                || block == Blocks.CHAIN_COMMAND_BLOCK || block == Blocks.STRUCTURE_BLOCK) {
            if (entity instanceof EntityLivingBase) {
                ((EntityLivingBase)entity).swingArm(EnumHand.MAIN_HAND);
            }
            world.destroyBlock(pos, false);
            if (!world.isRemote) {
                final EntityItem entityToSpawn = new EntityItem(world, (double)x, (double)y, (double)z, new ItemStack(block, 1));
                entityToSpawn.setPickupDelay(10);
                world.spawnEntity((Entity)entityToSpawn);
            }
            final ItemStack _ist = itemstack;
            if (_ist.attemptDamageItem(1, new Random(), (EntityPlayerMP)null)) {
                _ist.shrink(1);
                _ist.setItemDamage(0);
            }
        }
        else {
            if (entity instanceof EntityPlayer) {
                ((EntityPlayer) entity).sendMessage(new TextComponentString("该方块无法拆除！"));
            }
        }
    }
}


