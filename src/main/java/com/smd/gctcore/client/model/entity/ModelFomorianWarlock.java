package com.smd.gctcore.client.model.entity;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.entity.EntityFomorianWarlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelFomorianWarlock extends GiantHeadLookModel<EntityFomorianWarlock> {

    private static final ResourceLocation MODEL = new ResourceLocation(
            Tags.MOD_ID, "geo/fomorian_warlock.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            Tags.MOD_ID, "textures/entity/fomorian_warlock.png");
    private static final ResourceLocation BROKEN_RUNE_TEXTURE = new ResourceLocation(
            Tags.MOD_ID, "textures/entity/fomorian_warlock_rune_broken.png");
    private static final ResourceLocation ANIMATIONS = new ResourceLocation(
            Tags.MOD_ID, "animations/fomorian_warlock.animation.json");

    @Override
    public ResourceLocation getModelLocation(EntityFomorianWarlock entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityFomorianWarlock entity) {
        return entity.isBloodRuneBroken() ? BROKEN_RUNE_TEXTURE : TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationFileLocation(EntityFomorianWarlock entity) {
        return ANIMATIONS;
    }
}
