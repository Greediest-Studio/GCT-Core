package com.smd.gctcore.common.util;

import github.kasuminova.mmce.common.util.DynamicPattern;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import ink.ikx.mmce.common.utils.FluidUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MMCEBuilderUtils {

    private MMCEBuilderUtils() {
    }

    public static NBTTagCompound getOrCreateTag(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        return stack.getTagCompound();
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static void sendTranslation(EntityPlayer player, String key, Object... args) {
        player.sendMessage(new TextComponentTranslation(key, args));
    }

    public static String posToString(BlockPos pos) {
        return hellfirepvp.modularmachinery.common.util.MiscUtils.posToString(pos);
    }

    public static boolean isReplaceableForAssembly(World world, BlockPos pos) {
        IBlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        return world.isAirBlock(pos) || block instanceof IPlantable || block instanceof BlockLiquid || block instanceof IFluidBlock;
    }

    public static List<IFluidHandlerItem> getFluidHandlerItems(List<ItemStack> inventory) {
        List<IFluidHandlerItem> fluidHandlers = new ArrayList<>();
        for (ItemStack invStack : inventory) {
            Item item = invStack.getItem();
            if (item instanceof UniversalBucket || item == Items.LAVA_BUCKET || item == Items.WATER_BUCKET) {
                continue;
            }
            if (!FluidUtils.isFluidHandler(invStack)) {
                continue;
            }
            IFluidHandlerItem handler = FluidUtil.getFluidHandler(invStack);
            if (handler != null) {
                fluidHandlers.add(handler);
            }
        }
        return fluidHandlers;
    }

    public static void appendDynamicPatterns(DynamicMachine machine, BlockArray machinePattern, EnumFacing controllerFacing, int requestedLength) {
        Map<String, DynamicPattern> dynamicPatterns = machine.getDynamicPatterns();
        int length = requestedLength;
        for (DynamicPattern pattern : dynamicPatterns.values()) {
            length = Math.max(length, pattern.getMinSize());
        }
        for (DynamicPattern pattern : dynamicPatterns.values()) {
            int clamped = Math.min(Math.max(pattern.getMinSize(), length), pattern.getMaxSize());
            pattern.addPatternToBlockArray(machinePattern, clamped, pattern.getFaces().iterator().next(), controllerFacing);
        }
    }
}
