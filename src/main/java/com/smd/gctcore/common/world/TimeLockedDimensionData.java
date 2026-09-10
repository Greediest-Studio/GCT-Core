package com.smd.gctcore.common.world;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;

public class TimeLockedDimensionData extends WorldSavedData {

    private static final String NAME = "gctcore_time_locked_dimensions";
    private static final String TAG_DIMENSIONS = "dimensions";
    private static final String TAG_DIM = "dim";
    private static final String TAG_DAY_OFFSET = "dayOffset";

    private final Map<Integer, Integer> dayOffsets = new HashMap<>();

    public TimeLockedDimensionData() {
        super(NAME);
    }

    public TimeLockedDimensionData(String name) {
        super(name);
    }

    public static TimeLockedDimensionData get(World world) {
        World saveWorld = DimensionManager.getWorld(0);
        if (saveWorld == null) {
            saveWorld = world;
        }
        if (saveWorld == null) {
            return null;
        }

        MapStorage storage = saveWorld.getMapStorage();
        if (storage == null) {
            return null;
        }

        TimeLockedDimensionData data = (TimeLockedDimensionData) storage.getOrLoadData(TimeLockedDimensionData.class, NAME);
        if (data == null) {
            data = new TimeLockedDimensionData();
            storage.setData(NAME, data);
        }
        return data;
    }

    public int getDayOffset(int dimension) {
        Integer value = dayOffsets.get(dimension);
        return value == null ? 0 : value;
    }

    public void setDayOffset(int dimension, int dayOffset) {
        int clamped = Math.max(0, dayOffset);
        if (getDayOffset(dimension) == clamped) {
            return;
        }
        if (clamped == 0) {
            dayOffsets.remove(dimension);
        } else {
            dayOffsets.put(dimension, clamped);
        }
        markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        dayOffsets.clear();
        NBTTagList list = nbt.getTagList(TAG_DIMENSIONS, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int dayOffset = Math.max(0, tag.getInteger(TAG_DAY_OFFSET));
            if (dayOffset > 0) {
                dayOffsets.put(tag.getInteger(TAG_DIM), dayOffset);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<Integer, Integer> entry : dayOffsets.entrySet()) {
            if (entry.getValue() == null || entry.getValue() <= 0) {
                continue;
            }
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger(TAG_DIM, entry.getKey());
            tag.setInteger(TAG_DAY_OFFSET, entry.getValue());
            list.appendTag(tag);
        }
        compound.setTag(TAG_DIMENSIONS, list);
        return compound;
    }
}
