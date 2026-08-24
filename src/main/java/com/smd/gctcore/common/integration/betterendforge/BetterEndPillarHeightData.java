package com.smd.gctcore.common.integration.betterendforge;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldSavedData;

import java.util.HashMap;
import java.util.Map;

/**
 * Persistent replacement for BetterEndForge's in-memory-only pillar height map.
 */
public final class BetterEndPillarHeightData extends WorldSavedData {

    public static final String NAME = "gctcore_betterend_pillar_heights";
    private static final String HEIGHTS_TAG = "Heights";

    private final Map<String, Integer> heights = new HashMap<>();

    public BetterEndPillarHeightData() {
        super(NAME);
    }

    public BetterEndPillarHeightData(String name) {
        super(name);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        heights.clear();
        NBTTagCompound heightsTag = compound.getCompoundTag(HEIGHTS_TAG);
        for (String key : heightsTag.getKeySet()) {
            if (heightsTag.hasKey(key, 3)) {
                int height = heightsTag.getInteger(key);
                if (isValidHeight(height)) {
                    heights.put(key, height);
                }
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound heightsTag = new NBTTagCompound();
        for (Map.Entry<String, Integer> entry : heights.entrySet()) {
            heightsTag.setInteger(entry.getKey(), entry.getValue());
        }
        compound.setTag(HEIGHTS_TAG, heightsTag);
        return compound;
    }

    public boolean hasHeight(String key) {
        return heights.containsKey(key);
    }

    public int getHeight(String key) {
        Integer height = heights.get(key);
        return height == null ? -1 : height;
    }

    public void setHeight(String key, int height) {
        if (!isValidHeight(height)) {
            return;
        }

        Integer previous = heights.put(key, height);
        if (previous == null || previous != height) {
            markDirty();
        }
    }

    public static BetterEndPillarHeightData get(WorldServer world) {
        BetterEndPillarHeightData data = (BetterEndPillarHeightData) world.getPerWorldStorage()
                .getOrLoadData(BetterEndPillarHeightData.class, NAME);
        if (data == null) {
            data = new BetterEndPillarHeightData();
            world.getPerWorldStorage().setData(NAME, data);
        }
        return data;
    }

    private static boolean isValidHeight(int height) {
        return height >= 0 && height < 256;
    }
}
