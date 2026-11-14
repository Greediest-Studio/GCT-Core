package com.smd.gctcore.client.integration.stellarcore;

import com.smd.gctcore.gctcore;

/**
 * Coordinates post-load freezing of StellarCore's global quad and unpacked-data pools.
 * Startup model baking keeps full deduplication; once loadComplete triggers reset(),
 * runtime lazy quad allocations no longer re-populate those static sets.
 */
public final class StellarQuadPoolHelper {

    private static volatile boolean poolsFrozen = false;

    private StellarQuadPoolHelper() {
    }

    public static boolean isPoolsFrozen() {
        return poolsFrozen;
    }

    public static void freezePools() {
        if (!poolsFrozen) {
            poolsFrozen = true;
            gctcore.LOGGER.info("[GCT-StellarPoolFreeze] Freezing StellarCore UnpackedDataPool, VertexDataPool, and BakedQuadPool after load reset.");
        }
    }
}
