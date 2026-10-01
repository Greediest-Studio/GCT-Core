package com.gmm.gctold.common.compat;

import java.lang.reflect.Method;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

public final class NaturesAuraCompat {
    private static final String MOD_ID = "naturesaura";
    private static final int DEFAULT_RADIUS = 1;
    private static final String AURA_CHUNK_CLASS = "de.ellpeck.naturesaura.api.aura.chunk.IAuraChunk";

    private NaturesAuraCompat() {
    }

    public static void drainAura(World world, BlockPos pos, int amount) {
        if (world.isRemote || !Loader.isModLoaded(MOD_ID)) {
            return;
        }

        try {
            Class<?> auraChunkClass = Class.forName(AURA_CHUNK_CLASS);
            Method getHighestSpot = auraChunkClass.getMethod("getHighestSpot", World.class, BlockPos.class, int.class,
                    BlockPos.class);
            Method getAuraChunk = auraChunkClass.getMethod("getAuraChunk", World.class, BlockPos.class);
            Method drainAura = auraChunkClass.getMethod("drainAura", BlockPos.class, int.class);

            int remaining = amount;
            while (remaining > 0) {
                BlockPos drainSpot = (BlockPos) getHighestSpot.invoke(null, world, pos, DEFAULT_RADIUS, pos);
                Object auraChunk = getAuraChunk.invoke(null, world, drainSpot);
                int drained = (Integer) drainAura.invoke(auraChunk, drainSpot, remaining);
                if (drained <= 0) {
                    return;
                }
                remaining -= drained;
            }
        } catch (ReflectiveOperationException | LinkageError ignored) {
        }
    }
}
