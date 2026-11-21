package com.smd.gctcore.common.world.jotunheim;

import com.smd.gctcore.common.world.biome.nilfheim.NilfheimBiomes;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;

/** Fixed ID by design: never silently move an existing save to another dimension. */
public final class JotunheimDimension {
    public static final int DIMENSION_ID = NilfheimBiomes.DIMENSION_ID + 1;
    public static DimensionType TYPE;

    private JotunheimDimension() {}

    public static synchronized void register() {
        if (DimensionManager.isDimensionRegistered(DIMENSION_ID)) {
            if (TYPE != null && DimensionManager.getProviderType(DIMENSION_ID) == TYPE) return;
            throw new IllegalStateException("Cannot register Jotunheim: fixed dimension ID " + DIMENSION_ID
                    + " (Nilfheim + 1) is already registered by another provider. Resolve the ID conflict before loading a world.");
        }
        if (TYPE == null) {
            for (DimensionType existing : DimensionType.values()) {
                if (existing.getId() == DIMENSION_ID) {
                    throw new IllegalStateException("Cannot register Jotunheim: dimension type ID " + DIMENSION_ID
                            + " is already used by " + existing.getName() + ". Jotunheim will not select a different ID.");
                }
            }
            TYPE = DimensionType.register("jotunheim", "_jotunheim", DIMENSION_ID, WorldProviderJotunheim.class, false);
        }
        DimensionManager.registerDimension(DIMENSION_ID, TYPE);
    }
}
