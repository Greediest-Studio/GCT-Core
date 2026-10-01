package com.gmm.gctold.common.world.structure;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SurfaceTemplateStructure extends TemplateStructureFeature {
    private final boolean netherStyle;
    private final int yOffset;

    protected SurfaceTemplateStructure(int dimensionId, int chance, String templateName) {
        this(dimensionId, chance, templateName, false, 0);
    }

    protected SurfaceTemplateStructure(int dimensionId, int chance, StructureTemplateId templateId) {
        this(dimensionId, chance, templateId, false, 0);
    }

    protected SurfaceTemplateStructure(int dimensionId, int chance, String templateName, boolean netherStyle,
            int yOffset) {
        this(dimensionId, chance, templateName, netherStyle, yOffset, 1);
    }

    protected SurfaceTemplateStructure(int dimensionId, int chance, StructureTemplateId templateId, boolean netherStyle,
            int yOffset) {
        this(dimensionId, chance, templateId, netherStyle, yOffset, 1);
    }

    protected SurfaceTemplateStructure(int dimensionId, int chance, String templateName, boolean netherStyle,
            int yOffset, int maxAttempts) {
        super(dimensionId, chance, templateName, maxAttempts);
        this.netherStyle = netherStyle;
        this.yOffset = yOffset;
    }

    protected SurfaceTemplateStructure(int dimensionId, int chance, StructureTemplateId templateId, boolean netherStyle,
            int yOffset, int maxAttempts) {
        super(dimensionId, chance, templateId, maxAttempts);
        this.netherStyle = netherStyle;
        this.yOffset = yOffset;
    }

    protected SurfaceTemplateStructure(int dimensionId, int chance, String templateName, boolean netherStyle,
            int yOffset, @Nullable Rotation rotation, @Nullable Mirror mirror) {
        super(dimensionId, chance, templateName, rotation, mirror);
        this.netherStyle = netherStyle;
        this.yOffset = yOffset;
    }

    protected SurfaceTemplateStructure(int dimensionId, int chance, StructureTemplateId templateId, boolean netherStyle,
            int yOffset, @Nullable Rotation rotation, @Nullable Mirror mirror) {
        super(dimensionId, chance, templateId, rotation, mirror);
        this.netherStyle = netherStyle;
        this.yOffset = yOffset;
    }

    @Override
    protected BlockPos findOrigin(Random random, int chunkX, int chunkZ, World world) {
        return findSurfaceOrigin(random, chunkX, chunkZ, world, netherStyle).add(0, yOffset, 0);
    }
}
