package com.gmm.gctold.common.util;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.Teleporter;

public final class PlayerDimensionTransferHelper {
    private PlayerDimensionTransferHelper() {
    }

    public static void transfer(EntityPlayerMP player, int dimension, Teleporter teleporter) {
        player.changeDimension(dimension, teleporter);
    }
}
