package com.smd.gctcore.client.model.entity;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.entity.EntityCloudGiantCrossbowman;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;

@SideOnly(Side.CLIENT)
public class ModelCloudGiantCrossbowman extends AnimatedGeoModel<EntityCloudGiantCrossbowman> {

    private static final ResourceLocation MODEL = new ResourceLocation(
            Tags.MOD_ID, "geo/cloud_giant_crossbowman.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            Tags.MOD_ID, "textures/entity/cloud_giant_crossbowman.png");
    private static final ResourceLocation ANIMATIONS = new ResourceLocation(
            Tags.MOD_ID, "animations/cloud_giant_crossbowman.animation.json");

    @Override
    public ResourceLocation getModelLocation(EntityCloudGiantCrossbowman entity) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCloudGiantCrossbowman entity) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationFileLocation(EntityCloudGiantCrossbowman entity) {
        return ANIMATIONS;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void setLivingAnimations(EntityCloudGiantCrossbowman entity, Integer uniqueId,
                                    AnimationEvent animationEvent) {
        super.setLivingAnimations(entity, uniqueId, animationEvent);

        IBone bolt = getAnimationProcessor().getBone("loaded_bolt");
        if (bolt != null) {
            // Reload and shoot keyframes control their own visibility through scale.
            // Reset hidden every render: one model instance serves multiple entities.
            bolt.setHidden(!entity.isBoltLoaded() && !entity.isReloading() && !entity.isShooting());
        }
    }
}
