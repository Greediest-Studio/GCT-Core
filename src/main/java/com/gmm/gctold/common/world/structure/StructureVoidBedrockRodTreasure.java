package com.gmm.gctold.common.world.structure;

import com.gmm.gctold.common.world.dimension.WorldTheVoid;
import com.gmm.gctold.common.world.biome.BiomeVoidHill;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

public class StructureVoidBedrockRodTreasure extends BlockFilteredSurfaceTemplateStructure {
    public StructureVoidBedrockRodTreasure() {
        super(WorldTheVoid.DIMID, 10000, GctAllStructureTemplates.VOID_BEDROCK_ROD_TREASURE, Blocks.BEDROCK, BlockPos.ORIGIN.up(),
                BiomeVoidHill.biome);
    }
}
