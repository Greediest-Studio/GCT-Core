package com.smd.gctcore.common.integration.mmce;

import com.smd.gctcore.common.util.MMCEBuilderUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public final class MMCE_BuilderConfig {

    private static final String TAG_USE_AE_ITEMS = "gct_mmce_use_ae_items";
    private static final String TAG_USE_AE_FLUIDS = "gct_mmce_use_ae_fluids";
    private static final String TAG_CRAFT_MISSING = "gct_mmce_craft_missing";
    private static final String TAG_LEGACY_CRAFT_MISSING_ITEMS = "gct_mmce_craft_missing_items";
    private static final String TAG_LEGACY_CRAFT_MISSING_FLUIDS = "gct_mmce_craft_missing_fluids";
    private static final String TAG_DYNAMIC_LENGTH = "gct_mmce_dynamic_length";
    private static final int DEFAULT_DYNAMIC_LENGTH = 1;
    private static final int MAX_DYNAMIC_LENGTH = 4096;
    public static final int TICK_INTERVAL = 1;
    public static final int OPERATIONS_PER_TICK = 64;

    private MMCE_BuilderConfig() {
    }

    public static boolean useAeItems(ItemStack stack) {
        return getTag(stack).getBoolean(TAG_USE_AE_ITEMS);
    }

    public static void setUseAeItems(ItemStack stack, boolean value) {
        getTag(stack).setBoolean(TAG_USE_AE_ITEMS, value);
    }

    public static boolean useAeFluids(ItemStack stack) {
        return getTag(stack).getBoolean(TAG_USE_AE_FLUIDS);
    }

    public static void setUseAeFluids(ItemStack stack, boolean value) {
        getTag(stack).setBoolean(TAG_USE_AE_FLUIDS, value);
    }

    public static boolean craftMissing(ItemStack stack) {
        NBTTagCompound tag = getTag(stack);
        if (tag.hasKey(TAG_CRAFT_MISSING)) {
            return tag.getBoolean(TAG_CRAFT_MISSING);
        }
        return tag.getBoolean(TAG_LEGACY_CRAFT_MISSING_ITEMS) || tag.getBoolean(TAG_LEGACY_CRAFT_MISSING_FLUIDS);
    }

    public static void setCraftMissing(ItemStack stack, boolean value) {
        NBTTagCompound tag = getTag(stack);
        tag.setBoolean(TAG_CRAFT_MISSING, value);
        tag.removeTag(TAG_LEGACY_CRAFT_MISSING_ITEMS);
        tag.removeTag(TAG_LEGACY_CRAFT_MISSING_FLUIDS);
    }

    public static int dynamicLength(ItemStack stack) {
        NBTTagCompound tag = getTag(stack);
        if (!tag.hasKey(TAG_DYNAMIC_LENGTH)) {
            tag.setInteger(TAG_DYNAMIC_LENGTH, DEFAULT_DYNAMIC_LENGTH);
        }
        return clampDynamicLength(tag.getInteger(TAG_DYNAMIC_LENGTH));
    }

    public static void setDynamicLength(ItemStack stack, int value) {
        getTag(stack).setInteger(TAG_DYNAMIC_LENGTH, clampDynamicLength(value));
    }

    public static int clampDynamicLength(int value) {
        return MMCEBuilderUtils.clamp(value, 0, MAX_DYNAMIC_LENGTH);
    }

    private static NBTTagCompound getTag(ItemStack stack) {
        return MMCEBuilderUtils.getOrCreateTag(stack);
    }
}
