package com.smd.gctcore.common.mixin.aether;

import com.smd.gctcore.common.entity.EntityCloudGiantPriest;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

@Mixin(targets = "com.gildedgames.the_aether.entities.hostile.EntityWhirlwind", remap = false)
public abstract class MixinWhirlwindImmunity {

    @Redirect(
            method = "onLivingUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getEntitiesWithinAABBExcludingEntity(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/List;",
                    remap = true
            ),
            remap = true,
            require = 1
    )
    private List<Entity> gctcore$excludePriestFromWhirlwindInfluence(
            World world, Entity whirlwind, AxisAlignedBB bounds) {
        List<Entity> nearbyEntities = new ArrayList<>(
                world.getEntitiesWithinAABBExcludingEntity(whirlwind, bounds));
        nearbyEntities.removeIf(entity -> entity instanceof EntityCloudGiantPriest);
        return nearbyEntities;
    }
}
