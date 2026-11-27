package com.smd.gctcore.common.mixin.vanilla;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Replaces ModifiableAttributeInstance backing collections with concurrent
 * variants to prevent ConcurrentModificationException when attributes are
 * read (e.g., SPacketEntityProperties) while modified concurrently.
 */
@Mixin(ModifiableAttributeInstance.class)
public abstract class MixinModifiableAttributeInstance {

    @Shadow
    @Final
    @Mutable
    private Map<Integer, Set<AttributeModifier>> mapByOperation;

    @Shadow
    @Final
    @Mutable
    private Map<String, Set<AttributeModifier>> mapByName;

    @Shadow
    @Final
    @Mutable
    private Map<UUID, AttributeModifier> mapByUUID;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void gctcore$useConcurrentMaps(AbstractAttributeMap attributeMapIn, IAttribute genericAttributeIn, CallbackInfo ci) {
        Map<Integer, Set<AttributeModifier>> concurrentByOp = new ConcurrentHashMap<>();
        for (int i = 0; i < 3; ++i) {
            concurrentByOp.put(i, ConcurrentHashMap.newKeySet());
        }
        this.mapByOperation = concurrentByOp;
        this.mapByName = new ConcurrentHashMap<>();
        this.mapByUUID = new ConcurrentHashMap<>();
    }
}
