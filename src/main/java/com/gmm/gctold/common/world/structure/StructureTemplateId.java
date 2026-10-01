package com.gmm.gctold.common.world.structure;

import net.minecraft.util.ResourceLocation;

public final class StructureTemplateId {
    private final String name;
    private final ResourceLocation resourceLocation;

    public StructureTemplateId(String name) {
        this.name = name;
        this.resourceLocation = new ResourceLocation("gctold", name);
    }

    public String getName() {
        return name;
    }

    public ResourceLocation getResourceLocation() {
        return resourceLocation;
    }
}
