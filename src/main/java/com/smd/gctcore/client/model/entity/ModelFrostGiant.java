package com.smd.gctcore.client.model.entity;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.entity.EntityFrostGiant;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelFrostGiant extends GiantHeadLookModel<EntityFrostGiant> {
    private static final ResourceLocation MODEL = new ResourceLocation(Tags.MOD_ID, "geo/frost_giant.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MOD_ID, "textures/entity/frost_giant.png");
    private static final ResourceLocation ANIMATIONS = new ResourceLocation(Tags.MOD_ID, "animations/frost_giant.animation.json");

    @Override
    public ResourceLocation getModelLocation(EntityFrostGiant entity) { return MODEL; }

    @Override
    public ResourceLocation getTextureLocation(EntityFrostGiant entity) { return TEXTURE; }

    @Override
    public ResourceLocation getAnimationFileLocation(EntityFrostGiant entity) { return ANIMATIONS; }

}
