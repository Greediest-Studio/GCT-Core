package com.smd.gctcore.client.model.entity;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.entity.EntityCloudGiantPriest;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.model.AnimatedGeoModel;

@SideOnly(Side.CLIENT)
public class ModelCloudGiantPriest extends AnimatedGeoModel<EntityCloudGiantPriest> {

    private static final ResourceLocation MODEL = new ResourceLocation(
            Tags.MOD_ID, "geo/cloud_giant_wanderer.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            Tags.MOD_ID, "textures/entity/cloud_giant_wanderer.png");
    private static final ResourceLocation ANIMATIONS = new ResourceLocation(
            Tags.MOD_ID, "animations/cloud_giant_wanderer.animation.json");

    @Override
    public ResourceLocation getModelLocation(EntityCloudGiantPriest entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCloudGiantPriest entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationFileLocation(EntityCloudGiantPriest entity) {
        return ANIMATIONS;
    }
}
