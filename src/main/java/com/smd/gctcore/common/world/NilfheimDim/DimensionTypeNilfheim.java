package com.smd.gctcore.common.world.NilfheimDim;

import com.smd.gctcore.common.world.biome.nilfheim.NilfheimBiomes;

import static net.minecraft.world.DimensionType.register;

public class DimensionTypeNilfheim {
    public static final net.minecraft.world.DimensionType NILFHEIM;

    static {
        NILFHEIM = register("nilfheim", "_nilfheim", NilfheimBiomes.DIMENSION_ID, WorldProviderNilfheim.class, false);
    }
}
