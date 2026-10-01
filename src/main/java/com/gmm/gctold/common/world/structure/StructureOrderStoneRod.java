package com.gmm.gctold.common.world.structure;

import com.gmm.gctold.common.world.dimension.WorldOrderland;
import com.gmm.gctold.common.world.biome.BiomeOrderBasin;
import com.gmm.gctold.common.world.biome.BiomeOrderPlain;

public class StructureOrderStoneRod extends BiomeSurfaceTemplateStructure {
    public StructureOrderStoneRod() {
        super(WorldOrderland.DIMID, 100000, GctAllStructureTemplates.ORDER_ROD_1, 0, BiomeOrderBasin.biome, BiomeOrderPlain.biome);
    }
}
