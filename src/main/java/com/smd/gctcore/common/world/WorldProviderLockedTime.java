package com.smd.gctcore.common.world;

import net.minecraft.world.WorldProvider;

public abstract class WorldProviderLockedTime extends WorldProvider {

    public static final long DAY_LENGTH = 24000L;

    private final long frozenTimeOfDay;
    private boolean timeLocked = true;
    private int dayOffset;
    private boolean dayOffsetLoaded;

    protected WorldProviderLockedTime(long frozenTimeOfDay) {
        this.frozenTimeOfDay = frozenTimeOfDay;
    }
    public long getFrozenTimeOfDay() {
        return frozenTimeOfDay;
    }

    public int getDayOffset() {
        if (!dayOffsetLoaded && !world.isRemote) {
            TimeLockedDimensionData data = TimeLockedDimensionData.get(world);
            if (data != null) {
                this.dayOffset = data.getDayOffset(getDimension());
                this.dayOffsetLoaded = true;
            }
        }
        return dayOffset;
    }

    public void setDayOffset(int dayOffset) {
        this.dayOffset = Math.max(0, dayOffset);
        this.dayOffsetLoaded = true;
        if (!world.isRemote) {
            TimeLockedDimensionData data = TimeLockedDimensionData.get(world);
            if (data != null) {
                data.setDayOffset(getDimension(), this.dayOffset);
            }
        }
    }

    public int getDayNumber() {
        return getDayOffset() + 1;
    }

    public boolean isTimeLocked() {
        return timeLocked;
    }

    public void setTimeLocked(boolean timeLocked) {
        this.timeLocked = timeLocked;
    }

    public boolean isFrozenDaytime() {
        return frozenTimeOfDay % DAY_LENGTH < 12000L;
    }

    @Override
    public long getWorldTime() {
        if (!timeLocked) {
            return world.getWorldInfo().getWorldTime();
        }
        return frozenTimeOfDay + (long) getDayOffset() * DAY_LENGTH;
    }
}
