package com.smd.gctcore.client.model.entity;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.entity.EntityFomorianNoble;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.model.AnimatedGeoModel;

@SideOnly(Side.CLIENT)
public class ModelFomorianNoble extends AnimatedGeoModel<EntityFomorianNoble> {

    private static final ResourceLocation MODEL = new ResourceLocation(
            Tags.MOD_ID, "geo/fomorian_noble.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            Tags.MOD_ID, "textures/entity/fomorian_noble.png");
    private static final ResourceLocation ANIMATIONS = new ResourceLocation(
            Tags.MOD_ID, "animations/fomorian_noble.animation.json");

    @Override
    public ResourceLocation getModelLocation(EntityFomorianNoble entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityFomorianNoble entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationFileLocation(EntityFomorianNoble entity) {
        return ANIMATIONS;
    }
}
