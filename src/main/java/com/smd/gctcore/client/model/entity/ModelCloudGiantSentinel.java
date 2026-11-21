package com.smd.gctcore.client.model.entity;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.entity.EntityCloudGiantSentinel;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.model.AnimatedGeoModel;

@SideOnly(Side.CLIENT)
public class ModelCloudGiantSentinel extends AnimatedGeoModel<EntityCloudGiantSentinel> {

    private static final ResourceLocation MODEL = new ResourceLocation(
            Tags.MOD_ID, "geo/cloud_giant_sentinel.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            Tags.MOD_ID, "textures/entity/cloud_giant_sentinel.png");
    private static final ResourceLocation ANIMATIONS = new ResourceLocation(
            Tags.MOD_ID, "animations/cloud_giant_sentinel.animation.json");

    @Override
    public ResourceLocation getModelLocation(EntityCloudGiantSentinel entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCloudGiantSentinel entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationFileLocation(EntityCloudGiantSentinel entity) {
        return ANIMATIONS;
    }
}
