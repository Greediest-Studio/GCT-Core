package com.wdcftgg.witherstormmod.api.common.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

// Compile-time stub only - excluded from packaged jar by build.gradle
public abstract class SickenedMobConversionEvent extends Event {
    private final EntityLivingBase original;
    private final EntityLivingBase replacement;
    private final Direction direction;

    protected SickenedMobConversionEvent(EntityLivingBase original,
                                         EntityLivingBase replacement,
                                         Direction direction) {
        this.original = original;
        this.replacement = replacement;
        this.direction = direction;
    }

    public EntityLivingBase getOriginal() {
        return original;
    }

    public EntityLivingBase getReplacement() {
        return replacement;
    }

    public Direction getDirection() {
        return direction;
    }

    public enum Direction {
        INFECTION,
        CURE
    }

    @Cancelable
    public static final class Pre extends SickenedMobConversionEvent {
        public Pre(EntityLivingBase original, EntityLivingBase replacement, Direction direction) {
            super(original, replacement, direction);
        }
    }

    public static final class Post extends SickenedMobConversionEvent {
        public Post(EntityLivingBase original, EntityLivingBase replacement, Direction direction) {
            super(original, replacement, direction);
        }
    }
}

