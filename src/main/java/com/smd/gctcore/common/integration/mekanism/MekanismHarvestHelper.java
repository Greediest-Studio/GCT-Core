package com.smd.gctcore.common.integration.mekanism;

import com.smd.gctcore.common.config.GCTCompatConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Shared harvest-level helpers for Mekanism balance mixins.
 */
public final class MekanismHarvestHelper {

    /** Tile NBT key for persisted mining level. */
    public static final String TILE_MINING_LEVEL_KEY = "gctMiningLevel";

    private MekanismHarvestHelper() {
    }

    public static String miningLevelNbtKey() {
        return GCTCompatConfig.mekanismIntegration.miningLevelNbtKey;
    }

    /**
     * Reads harvest level from an upgrade item stack NBT.
     * Missing tag / empty stack → 0.
     */
    public static int readMiningLevelFromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTagCompound()) {
            return 0;
        }
        NBTTagCompound tag = stack.getTagCompound();
        String key = miningLevelNbtKey();
        if (tag == null || !tag.hasKey(key)) {
            return 0;
        }
        try {
            return Math.max(0, tag.getInteger(key));
        } catch (Throwable ignored) {
            return 0;
        }
    }

    /**
     * Writes harvest level onto an upgrade item stack (creates NBT if needed).
     */
    public static ItemStack writeMiningLevelToStack(ItemStack stack, int level) {
        if (stack == null || stack.isEmpty()) {
            return stack;
        }
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setInteger(miningLevelNbtKey(), Math.max(0, level));
        return stack;
    }

    public static int getBlockHarvestLevel(IBlockState state) {
        try {
            return state.getBlock().getHarvestLevel(state);
        } catch (Throwable ignored) {
            return -1;
        }
    }

    public static boolean canLaserMine(IBlockState state) {
        if (!GCTCompatConfig.mekanismIntegration.enableLaserHarvestLimit) {
            return true;
        }
        return getBlockHarvestLevel(state) <= GCTCompatConfig.mekanismIntegration.maxLaserHarvestLevel;
    }

    public static boolean canDigitalMinerMine(IBlockState state, int machineLevel) {
        if (!GCTCompatConfig.mekanismIntegration.enableDigitalMinerHarvestLimit) {
            return true;
        }
        return getBlockHarvestLevel(state) <= machineLevel;
    }
}
