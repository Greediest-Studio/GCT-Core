package com.smd.gctcore.client.model.entity;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.entity.EntityFireGiant;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelFireGiant extends GiantHeadLookModel<EntityFireGiant> {
    private static final ResourceLocation MODEL = new ResourceLocation(Tags.MOD_ID, "geo/fire_giant.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MOD_ID, "textures/entity/fire_giant.png");
    private static final ResourceLocation ANIMATIONS = new ResourceLocation(Tags.MOD_ID, "animations/fire_giant.animation.json");

    @Override
    public ResourceLocation getModelLocation(EntityFireGiant entity) { return MODEL; }

    @Override
    public ResourceLocation getTextureLocation(EntityFireGiant entity) { return TEXTURE; }

    @Override
    public ResourceLocation getAnimationFileLocation(EntityFireGiant entity) { return ANIMATIONS; }
}
