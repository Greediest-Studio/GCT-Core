package com.smd.gctcore.client.model.entity;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.entity.EntityCloudGiantCrystal;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.model.AnimatedGeoModel;

@SideOnly(Side.CLIENT)
public class ModelCloudGiantCrystal extends AnimatedGeoModel<EntityCloudGiantCrystal> {

    private static final ResourceLocation MODEL = new ResourceLocation(
            Tags.MOD_ID, "geo/cloud_giant_crystal.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            Tags.MOD_ID, "textures/entity/cloud_giant_crystal.png");
    private static final ResourceLocation ANIMATIONS = new ResourceLocation(
            Tags.MOD_ID, "animations/cloud_giant_crystal.animation.json");

    @Override
    public ResourceLocation getModelLocation(EntityCloudGiantCrystal entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCloudGiantCrystal entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationFileLocation(EntityCloudGiantCrystal entity) {
        return ANIMATIONS;
    }
}
