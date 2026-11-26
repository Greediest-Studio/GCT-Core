package com.smd.gctcore.client.model.entity;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.entity.EntityEttin;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.builder.Animation;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

@SideOnly(Side.CLIENT)
public class ModelEttin extends AnimatedGeoModel<EntityEttin> {
    private static final ResourceLocation MODEL = new ResourceLocation(Tags.MOD_ID, "geo/ettin.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(Tags.MOD_ID, "textures/entity/ettin.png");
    private static final ResourceLocation ANIMATIONS = new ResourceLocation(Tags.MOD_ID, "animations/ettin.animation.json");
    private static final String[] HEAD_NAMES = {"head_left", "head_right"};
    private static final float DEGREES_TO_RADIANS = (float) Math.PI / 180.0F;
    private final Map<EntityEttin, HeadPose> sampledHeads = new WeakHashMap<>();
    private final IBone[] renderedHeads = new IBone[HEAD_NAMES.length];
    private final float[] sampledPitch = new float[HEAD_NAMES.length];
    private final float[] sampledYaw = new float[HEAD_NAMES.length];

    @Override
    public ResourceLocation getModelLocation(EntityEttin entity) { return MODEL; }

    @Override
    public ResourceLocation getTextureLocation(EntityEttin entity) { return TEXTURE; }

    @Override
    public ResourceLocation getAnimationFileLocation(EntityEttin entity) { return ANIMATIONS; }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void setLivingAnimations(EntityEttin entity, Integer uniqueId, @Nullable AnimationEvent event) {
        // Restore both clean authored poses before GeckoLib samples this shared model
        // or snapshots a transition. Subtracting previous offsets accumulates error.
        for (int i = 0; i < renderedHeads.length; i++) {
            if (renderedHeads[i] != null) {
                renderedHeads[i].setRotationX(sampledPitch[i]);
                renderedHeads[i].setRotationY(sampledYaw[i]);
                renderedHeads[i] = null;
            }
        }
        super.setLivingAnimations(entity, uniqueId, event);
        if (event == null) { return; }

        IBone[] heads = new IBone[HEAD_NAMES.length];
        HeadPose previous = sampledHeads.get(entity);
        boolean skippedSampling = event.getController() == null && previous != null
                && previous.entity.get() == entity
                && Double.compare(previous.tick, event.getAnimationTick()) == 0;
        for (int i = 0; i < heads.length; i++) {
            heads[i] = getAnimationProcessor().getBone(HEAD_NAMES[i]);
            if (heads[i] == null) { continue; }
            // Repeated render passes at the same seek time may follow another Ettin
            // using these same bone instances. Recover this entity's own clean pose.
            if (skippedSampling && previous.bones[i] == heads[i]) {
                heads[i].setRotationX(previous.pitch[i]);
                heads[i].setRotationY(previous.yaw[i]);
            }
            sampledPitch[i] = heads[i].getRotationX();
            sampledYaw[i] = heads[i].getRotationY();
        }
        sampledHeads.put(entity, new HeadPose(entity, heads, event.getAnimationTick(), sampledPitch, sampledYaw));

        List<EntityModelData> modelData = event.getExtraDataOfType(EntityModelData.class);
        if (!entity.isEntityAlive() || modelData.isEmpty()) { return; }
        AnimationController controller = entity.getFactory().getOrCreateAnimationData(uniqueId)
                .getAnimationControllers().get("body");
        Animation animation = controller == null ? null : controller.getCurrentAnimation();
        float weight = getHeadLookWeight(entity, animation == null ? null : animation.animationName);
        if (weight <= 0.0F) { return; }
        EntityModelData look = modelData.get(0);
        float pitch = MathHelper.clamp(look.headPitch, -55.0F, 55.0F) * DEGREES_TO_RADIANS * weight;
        float yaw = MathHelper.clamp(MathHelper.wrapDegrees(look.netHeadYaw), -65.0F, 65.0F)
                * DEGREES_TO_RADIANS * weight;
        for (int i = 0; i < heads.length; i++) {
            if (heads[i] == null) { continue; }
            // Each head turns around its own neck pivot, preserving its individual
            // idle nod. The common head parent receives no extra look rotation.
            heads[i].setRotationX(sampledPitch[i] + pitch);
            heads[i].setRotationY(sampledYaw[i] + yaw);
            renderedHeads[i] = heads[i];
        }
    }

    protected float getHeadLookWeight(EntityEttin entity, @Nullable String animationName) {
        if (animationName == null || animationName.endsWith(".idle") || animationName.endsWith(".walk")) {
            return 1.0F;
        }
        if (animationName.endsWith(".intro_roar") || animationName.endsWith(".hurt")
                || animationName.endsWith(".death")) { return 0.0F; }
        return 0.25F;
    }

    private static final class HeadPose {
        final WeakReference<EntityEttin> entity;
        final IBone[] bones;
        final double tick;
        final float[] pitch;
        final float[] yaw;

        HeadPose(EntityEttin entity, IBone[] bones, double tick, float[] pitch, float[] yaw) {
            this.entity = new WeakReference<>(entity);
            this.bones = bones.clone();
            this.tick = tick;
            this.pitch = pitch.clone();
            this.yaw = yaw.clone();
        }
    }
}
