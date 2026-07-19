package com.smd.gctcore.common.integration.extrabotany;

/**
 * Runtime access to custom damage / target data injected into ExtraBotany's EntitySubspace.
 * Method names match Extra-Botany's extended EntitySubspace API.
 * Must NOT live in a mixin package.
 */
public interface IEntitySubspaceExt {

    float getDamage();

    void setDamage(float damage);

    float getTargetX();

    void setTargetX(float x);

    float getTargetY();

    void setTargetY(float y);

    float getTargetZ();

    void setTargetZ(float z);

    boolean getHasTarget();

    void setHasTarget(boolean hasTarget);

    void setTarget(double x, double y, double z);
}
