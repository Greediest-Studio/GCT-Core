package com.gmm.gctold.common.events;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class EffectHelper {
    private EffectHelper() {
    }

    public static void addEffect(EntityLivingBase entity, Potion potion, int ticks, int amplifier, boolean ambient, boolean particles) {
        if (entity == null || entity.world.isRemote || potion == null) {
            return;
        }
        entity.addPotionEffect(new PotionEffect(potion, ticks, amplifier, ambient, particles));
    }

    public static void addEffectToPlayers(World world, BlockPos center, double radius, Potion potion, int ticks, int amplifier, boolean ambient, boolean particles) {
        if (world == null || world.isRemote || potion == null) {
            return;
        }
        for (EntityPlayer player : playersWithin(world, center, radius)) {
            addEffect(player, potion, ticks, amplifier, ambient, particles);
        }
    }

    public static EntityPlayer nearestPlayer(World world, BlockPos center, double radius) {
        if (world == null) {
            return null;
        }
        EntityPlayer nearest = null;
        double bestDistance = radius * radius;
        for (EntityPlayer player : playersWithin(world, center, radius)) {
            double distance = player.getDistanceSq(center);
            if (distance <= bestDistance) {
                bestDistance = distance;
                nearest = player;
            }
        }
        return nearest;
    }

    public static void damageNearestPlayer(World world, BlockPos center, double radius, DamageSource source, float amount) {
        EntityPlayer player = nearestPlayer(world, center, radius);
        if (player != null && !player.world.isRemote) {
            player.attackEntityFrom(source, amount);
        }
    }

    public static void clearPotions(Class<? extends EntityLivingBase> type, World world, BlockPos center, double radius) {
        if (world == null || world.isRemote) {
            return;
        }
        for (EntityLivingBase entity : livingWithin(type, world, center, radius)) {
            entity.clearActivePotions();
        }
    }

    public static <T extends EntityLivingBase> List<T> livingWithin(Class<T> type, World world, BlockPos center, double radius) {
        AxisAlignedBB area = new AxisAlignedBB(center).grow(radius);
        return world.getEntitiesWithinAABB(type, area, entity -> entity.getDistanceSq(center) <= radius * radius);
    }

    public static List<EntityPlayer> playersWithin(World world, BlockPos center, double radius) {
        AxisAlignedBB area = new AxisAlignedBB(center).grow(radius);
        return world.getEntitiesWithinAABB(EntityPlayer.class, area, player -> player.getDistanceSq(center) <= radius * radius);
    }

    public static void applyToEntities(Class<? extends Entity> type, World world, BlockPos center, double radius, EntityAction action) {
        if (world == null || world.isRemote) {
            return;
        }
        AxisAlignedBB area = new AxisAlignedBB(center).grow(radius);
        for (Entity entity : world.getEntitiesWithinAABB(type, area, entity -> entity.getDistanceSq(center) <= radius * radius)) {
            action.apply(entity);
        }
    }

    public interface EntityAction {
        void apply(Entity entity);
    }
}
