package com.smd.gctcore.client.model.entity;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.builder.Animation;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

/** Adds vanilla looking to the authored head pose without changing animation snapshots. */
@SideOnly(Side.CLIENT)
public abstract class GiantHeadLookModel<T extends EntityLivingBase & IAnimatable>
        extends AnimatedGeoModel<T> {

    private static final float DEGREES_TO_RADIANS = (float) Math.PI / 180.0F;
    private final Map<T, HeadPose> sampledHeads = new WeakHashMap<>();
    private IBone renderedHead;
    private float sampledPitch;
    private float sampledYaw;

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void setLivingAnimations(T entity, Integer uniqueId, @Nullable AnimationEvent event) {
        // A model/bone instance is shared by every entity of its type. Restore the
        // exact animation pose before GeckoLib samples or creates transition data;
        // subtracting the previous offset would gradually lose float precision.
        if (renderedHead != null) {
            renderedHead.setRotationX(sampledPitch);
            renderedHead.setRotationY(sampledYaw);
            renderedHead = null;
        }
        super.setLivingAnimations(entity, uniqueId, event);

        IBone head = getAnimationProcessor().getBone("head");
        if (head == null || event == null) {
            return;
        }

        HeadPose previous = sampledHeads.get(entity);
        // GeckoLib skips animation sampling when the same entity is rendered
        // again at the same seek time (e.g. an extra render pass). Another giant
        // may have used these shared bones in between. Reuse this entity's own
        // clean head pose only when the processor actually skipped the event.
        if (event.getController() == null && previous != null && previous.entity.get() == entity
                && previous.bone == head
                && Double.compare(previous.tick, event.getAnimationTick()) == 0) {
            head.setRotationX(previous.pitch);
            head.setRotationY(previous.yaw);
        }
        sampledPitch = head.getRotationX();
        sampledYaw = head.getRotationY();
        sampledHeads.put(entity, new HeadPose(entity, head,
                event.getAnimationTick(), sampledPitch, sampledYaw));

        List<EntityModelData> modelData = event.getExtraDataOfType(EntityModelData.class);
        if (!entity.isEntityAlive() || modelData.isEmpty()) {
            return;
        }
        AnimationController controller = entity.getFactory().getOrCreateAnimationData(uniqueId)
                .getAnimationControllers().get("body");
        Animation animation = controller == null ? null : controller.getCurrentAnimation();
        float weight = MathHelper.clamp(getHeadLookWeight(entity,
                animation == null ? null : animation.animationName), 0.0F, 1.0F);
        if (weight <= 0.0F) {
            return;
        }

        EntityModelData look = modelData.get(0);
        // EntityModelData already has GeckoLib's coordinate sign conversion.
        // Add to the sampled pose so idle nods and authored attack turns survive.
        float pitch = MathHelper.clamp(look.headPitch, -55.0F, 55.0F);
        float yaw = MathHelper.clamp(MathHelper.wrapDegrees(look.netHeadYaw), -65.0F, 65.0F);
        head.setRotationX(sampledPitch + pitch * DEGREES_TO_RADIANS * weight);
        head.setRotationY(sampledYaw + yaw * DEGREES_TO_RADIANS * weight);
        renderedHead = head;
    }

    /** Authored actions keep their main pose; looking is strongest during idle/walk. */
    protected float getHeadLookWeight(T entity, @Nullable String animationName) {
        if (animationName == null || animationName.endsWith(".idle") || animationName.endsWith(".walk")) {
            return 1.0F;
        }
        if (animationName.endsWith(".death") || animationName.endsWith(".intro")
                || animationName.endsWith(".phase_transition") || animationName.endsWith(".hurt")
                || animationName.endsWith(".jump")) {
            return 0.0F;
        }
        if (animationName.endsWith(".aim")) {
            return 0.65F;
        }
        return 0.25F;
    }

    private static final class HeadPose {
        final WeakReference<EntityLivingBase> entity;
        final IBone bone;
        final double tick;
        final float pitch;
        final float yaw;

        HeadPose(EntityLivingBase entity, IBone bone, double tick, float pitch, float yaw) {
            this.entity = new WeakReference<>(entity);
            this.bone = bone;
            this.tick = tick;
            this.pitch = pitch;
            this.yaw = yaw;
        }
    }
}
