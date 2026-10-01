package com.gmm.gctold.common.world.structure;

import com.gmm.gctold.common.world.dimension.WorldOrderland;
import com.gmm.gctold.common.world.biome.BiomeOrderBasin;
import com.gmm.gctold.common.world.biome.BiomeOrderPlain;

public class StructureOrderStoneRod2 extends BiomeSurfaceTemplateStructure {
    public StructureOrderStoneRod2() {
        super(WorldOrderland.DIMID, 50000, GctAllStructureTemplates.ORDER_ROD_2, 0, BiomeOrderBasin.biome, BiomeOrderPlain.biome);
    }
}
