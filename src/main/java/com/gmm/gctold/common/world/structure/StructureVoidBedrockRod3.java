package com.gmm.gctold.common.world.structure;

import com.gmm.gctold.common.world.dimension.WorldTheVoid;
import com.gmm.gctold.common.world.biome.BiomeVoidHill;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

public class StructureVoidBedrockRod3 extends BlockFilteredSurfaceTemplateStructure {
    public StructureVoidBedrockRod3() {
        super(WorldTheVoid.DIMID, 100000, GctAllStructureTemplates.VOID_BEDROCK_ROD_3, Blocks.BEDROCK, BlockPos.ORIGIN.up(),
                BiomeVoidHill.biome);
    }
}
