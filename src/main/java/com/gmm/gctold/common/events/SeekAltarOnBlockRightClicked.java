package com.gmm.gctold.common.events;

import com.gmm.gctold.common.blocks.BlockAzathothiumOreComplex;
import com.gmm.gctold.common.blocks.BlockNyralathotepiumOreComplex;
import com.gmm.gctold.common.blocks.BlockShubniggurathiumOreComplex;
import com.gmm.gctold.common.blocks.BlockYogsothothiumOreComplex;
import com.gmm.gctold.misc.registry.GctAllItems;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class SeekAltarOnBlockRightClicked {
    private SeekAltarOnBlockRightClicked() {
    }

    public static void run(Entity entity, World world, int x, int y, int z) {
        BlockPos altarPos = new BlockPos(x, y, z);
        TileEntity altar = world.getTileEntity(altarPos);
        if (altar == null || !altar.getTileData().getBoolean("active") || altar.getTileData().getBoolean("work")) {
            return;
        }

        Block blockAbove = world.getBlockState(altarPos.up()).getBlock();
        String recipeName = getOreRecipe(blockAbove);
        if (recipeName == null && isHolding(entity, GctAllItems.SOUL_STEALER_SCROLL)) {
            recipeName = "zjarugoth_summon";
            if (!world.isRemote && entity instanceof EntityPlayer) {
                ((EntityPlayer) entity).inventory.clearMatchingItems(GctAllItems.SOUL_STEALER_SCROLL, -1, 1, null);
            }
        }

        if (recipeName != null && !world.isRemote) {
            altar.getTileData().setString("recipeName", recipeName);
            world.notifyBlockUpdate(altarPos, world.getBlockState(altarPos), world.getBlockState(altarPos), 3);
        }
    }

    private static String getOreRecipe(Block block) {
        if (block == BlockAzathothiumOreComplex.block) {
            return "azathothium_ore";
        }
        if (block == BlockNyralathotepiumOreComplex.block) {
            return "nyarlathotepium_ore";
        }
        if (block == BlockYogsothothiumOreComplex.block) {
            return "yogsothothium_ore";
        }
        if (block == BlockShubniggurathiumOreComplex.block) {
            return "shubniggurathium_ore";
        }
        return null;
    }

    private static boolean isHolding(Entity entity, Item item) {
        ItemStack stack = entity instanceof EntityLivingBase
                ? ((EntityLivingBase) entity).getHeldItemMainhand()
                : ItemStack.EMPTY;
        return stack.getItem() == item;
    }
}
