package com.smd.gctcore.common.mixin.thaumicconcilium;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Prevent spawning of the Crimson Pontifex portal when a curio is thrown into a stable rift.
 * Targets ThaumicConcilium's EntityHexRift by name to avoid compile-time dependency.
 */
@Pseudo
@Mixin(targets = {"com.keletu.thaumicconcilium.entity.EntityHexRift"}, remap = false)
public class MixinEntityHexRift {

    private static final String PONTIFEX_CLASS = "com.keletu.thaumicconcilium.entity.EntityCrimsonPontifex";
    private static final String PONTIFEX_PORTAL_CLASS = "com.keletu.thaumicconcilium.entity.EntityPontifexPortal";

    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    private boolean preventPontifexSpawn(World world, Entity entity) {
        if (entity != null) {
            String cls = entity.getClass().getName();
            if (PONTIFEX_PORTAL_CLASS.equals(cls) || PONTIFEX_CLASS.equals(cls)) {
                // swallow the spawn, effectively preventing the boss/portal from appearing
                return false;
            }
        }
        return world.spawnEntity(entity);
    }
}
