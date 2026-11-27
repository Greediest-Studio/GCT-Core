package com.smd.gctcore.common.mixin.incontrol;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

/**
 * Snapshots World#loadedEntityList in InControl's RuleCache$CachePerWorld#count
 * to prevent ConcurrentModificationException during natural spawn checks.
 */
@Mixin(targets = "mcjty.incontrol.rules.RuleCache$CachePerWorld", remap = false)
@Pseudo
public abstract class MixinRuleCachePerWorld {

    @Redirect(
            method = "count",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/World;field_72996_f:Ljava/util/List;"
            ),
            remap = false
    )
    private List<Entity> gctcore$snapshotLoadedEntityList(World world) {
        return new ArrayList<>(world.loadedEntityList);
    }
}
