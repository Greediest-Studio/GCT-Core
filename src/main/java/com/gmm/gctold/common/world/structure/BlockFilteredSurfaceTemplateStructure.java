package com.gmm.gctold.common.world.structure;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class BlockFilteredSurfaceTemplateStructure extends BiomeSurfaceTemplateStructure {
    private final Block requiredBlock;
    private final BlockPos requiredBlockOffset;

    protected BlockFilteredSurfaceTemplateStructure(int dimensionId, int chance, String templateName, Block requiredBlock,
            BlockPos requiredBlockOffset, Biome... biomes) {
        super(dimensionId, chance, templateName, 0, biomes);
        this.requiredBlock = requiredBlock;
        this.requiredBlockOffset = requiredBlockOffset;
    }

    protected BlockFilteredSurfaceTemplateStructure(int dimensionId, int chance, StructureTemplateId templateId,
            Block requiredBlock, BlockPos requiredBlockOffset, Biome... biomes) {
        super(dimensionId, chance, templateId, 0, biomes);
        this.requiredBlock = requiredBlock;
        this.requiredBlockOffset = requiredBlockOffset;
    }

    @Override
    protected boolean canGenerateAt(World world, BlockPos origin) {
        return super.canGenerateAt(world, origin) && blockAt(world, origin.add(requiredBlockOffset), requiredBlock);
    }
}
