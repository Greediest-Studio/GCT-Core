package com.smd.gctcore.common.mixin.vanilla;

import net.minecraft.entity.ai.attributes.AttributeMap;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Replaces AttributeMap#dirtyInstances with a concurrent set to prevent
 * ConcurrentModificationException when SPacketEntityProperties iterates dirty
 * attribute instances while another thread marks an attribute dirty.
 */
@Mixin(AttributeMap.class)
public abstract class MixinAttributeMap {

    @Shadow
    @Final
    @Mutable
    private Set<IAttributeInstance> dirtyInstances;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void gctcore$useConcurrentDirtySet(CallbackInfo ci) {
        this.dirtyInstances = ConcurrentHashMap.newKeySet();
    }
}
