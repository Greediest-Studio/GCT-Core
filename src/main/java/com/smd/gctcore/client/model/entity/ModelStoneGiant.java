package com.smd.gctcore.client.model.entity;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.entity.EntityStoneGiant;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.model.AnimatedGeoModel;

@SideOnly(Side.CLIENT)
public class ModelStoneGiant extends AnimatedGeoModel<EntityStoneGiant> {

    private static final ResourceLocation MODEL = new ResourceLocation(Tags.MOD_ID, "geo/stone_giant.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MOD_ID, "textures/entity/stone_giant.png");
    private static final ResourceLocation ANIMATIONS = new ResourceLocation(Tags.MOD_ID, "animations/stone_giant.animation.json");

    @Override
    public ResourceLocation getModelLocation(EntityStoneGiant entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityStoneGiant entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationFileLocation(EntityStoneGiant entity) {
        return ANIMATIONS;
    }
}
