package com.smd.gctcore.common.entity;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;

/** Ettins hunt other giants, who also recognize ettins as enemies. */
public final class EntityAIGiantRivalTarget extends EntityAINearestAttackableTarget<EntityLivingBase> {
    private final double acquireRange;

    public EntityAIGiantRivalTarget(EntityCreature giant, double acquireRange) {
        super(giant, EntityLivingBase.class, 10, true, false,
                target -> isRival(giant, target)
                        && giant.getDistanceSq(target) <= acquireRange * acquireRange);
        this.acquireRange = acquireRange;
    }

    static boolean isOtherGiant(EntityLivingBase entity) {
        // Cloud crystals and projectiles belong to giant factions but are not combatants.
        return entity instanceof EntityAnimatedGiant && !(entity instanceof EntityEttin)
                || entity instanceof EntityHillGiant || entity instanceof EntityCloudGiantPriest
                || entity instanceof EntityCloudGiantSentinel || entity instanceof EntityCloudGiantCrossbowman;
    }

    private static boolean isRival(EntityLivingBase giant, EntityLivingBase target) {
        return giant instanceof EntityEttin ? isOtherGiant(target)
                : isOtherGiant(giant) && target instanceof EntityEttin;
    }

    @Override
    public boolean shouldExecute() {
        EntityLivingBase current = taskOwner.getAttackTarget();
        // Preserve revenge targets and the cloud giants' manually selected player targets.
        return (current == null || !current.isEntityAlive() || taskOwner.isOnSameTeam(current))
                && super.shouldExecute();
    }

    @Override
    public boolean shouldContinueExecuting() {
        EntityLivingBase current = taskOwner.getAttackTarget();
        return (current == null || current == targetEntity) && super.shouldContinueExecuting();
    }

    @Override
    protected double getTargetDistance() {
        return taskOwner.getAttackTarget() == null ? acquireRange : super.getTargetDistance();
    }

    @Override
    public void resetTask() {
        EntityLivingBase replacement = taskOwner.getAttackTarget();
        if (replacement == targetEntity) { replacement = null; }
        super.resetTask();
        if (replacement != null) { taskOwner.setAttackTarget(replacement); }
    }
}
