package com.smd.gctcore.client.model.entity;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.entity.EntityHillGiant;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelHillGiant extends GiantHeadLookModel<EntityHillGiant> {

    private static final ResourceLocation MODEL = new ResourceLocation(
            Tags.MOD_ID, "geo/hill_giant.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            Tags.MOD_ID, "textures/entity/hill_giant.png");
    private static final ResourceLocation ANIMATIONS = new ResourceLocation(
            Tags.MOD_ID, "animations/hill_giant.animation.json");

    @Override
    public ResourceLocation getModelLocation(EntityHillGiant entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityHillGiant entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationFileLocation(EntityHillGiant entity) {
        return ANIMATIONS;
    }
}
