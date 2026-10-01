package com.gmm.gctold.common.world.structure;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class BiomeSurfaceTemplateStructure extends SurfaceTemplateStructure {
    private final Biome[] biomes;

    protected BiomeSurfaceTemplateStructure(int dimensionId, int chance, String templateName, int yOffset,
            Biome... biomes) {
        super(dimensionId, chance, templateName, false, yOffset);
        this.biomes = biomes;
    }

    protected BiomeSurfaceTemplateStructure(int dimensionId, int chance, StructureTemplateId templateId, int yOffset,
            Biome... biomes) {
        super(dimensionId, chance, templateId, false, yOffset);
        this.biomes = biomes;
    }

    protected BiomeSurfaceTemplateStructure(int dimensionId, int chance, String templateName, int yOffset,
            @Nullable Rotation rotation, @Nullable Mirror mirror, Biome... biomes) {
        super(dimensionId, chance, templateName, false, yOffset, rotation, mirror);
        this.biomes = biomes;
    }

    protected BiomeSurfaceTemplateStructure(int dimensionId, int chance, StructureTemplateId templateId, int yOffset,
            @Nullable Rotation rotation, @Nullable Mirror mirror, Biome... biomes) {
        super(dimensionId, chance, templateId, false, yOffset, rotation, mirror);
        this.biomes = biomes;
    }

    @Override
    protected boolean canGenerateAt(World world, BlockPos origin) {
        return hasBiome(world, origin, biomes);
    }
}
