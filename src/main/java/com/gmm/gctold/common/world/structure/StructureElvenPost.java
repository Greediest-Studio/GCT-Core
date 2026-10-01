package com.gmm.gctold.common.world.structure;

import com.gmm.gctold.common.world.biome.BiomeAlfheimPlain;
import com.gmm.gctold.common.world.dimension.WorldAlfheim;

public class StructureElvenPost extends BiomeSurfaceTemplateStructure {
    public StructureElvenPost() {
        super(WorldAlfheim.DIMID, 10000, GctAllStructureTemplates.ELVEN_POST, 0, BiomeAlfheimPlain.biome);
    }
}
