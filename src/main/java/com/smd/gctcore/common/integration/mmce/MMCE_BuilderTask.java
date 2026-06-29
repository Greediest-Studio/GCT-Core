package com.smd.gctcore.common.integration.mmce;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface MMCE_BuilderTask {

    default void beginBatch() {}

    default void endBatch() {}

    World getWorld();

    BlockPos getCtrlPos();

    EntityPlayer getPlayer();

    int getTickInterval();

    int getOperationsPerTick();

    boolean isControllerInvalid();

    boolean isCompleted();

    default boolean isCancelled() {
        return false;
    }

    default void cancel() {}

    void tick();

    void report();

    String getCancelledMessageKey();

    String getSuccessMessageKey();
}
