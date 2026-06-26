package com.smd.gctcore.common.mixin.betterhurttimer;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Pseudo
@Mixin(targets = "arekkuusu.betterhurttimer.common.Events", remap = false)
public abstract class MixinEvents {

    @Redirect(
            method = "onNonLivingEntityUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;iterator()Ljava/util/Iterator;",
                    ordinal = 0
            ),
            require = 1,
            remap = false
    )
    private static Iterator<Entity> gctcore$snapshotLoadedEntityList(List<Entity> loadedEntityList) {
        return new ArrayList<>(loadedEntityList).iterator();
    }
}
