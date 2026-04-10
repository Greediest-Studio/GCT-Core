package com.smd.gctcore.common.mixin.astralsorcery;

import com.smd.gctcore.common.config.GctCoreConfig;
import hellfirepvp.astralsorcery.common.constellation.perk.PlayerAttributeMap;
import hellfirepvp.astralsorcery.common.constellation.perk.attribute.AttributeTypeRegistry;
import hellfirepvp.astralsorcery.common.constellation.perk.attribute.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(PlayerAttributeMap.class)
public abstract class MixinPlayerAttributeMap {

    @Inject(
            method = "modifyValue(Lnet/minecraft/entity/player/EntityPlayer;Lhellfirepvp/astralsorcery/common/data/research/PlayerProgress;Ljava/lang/String;F)F",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void onModifyValue(EntityPlayer player, PlayerProgress progress, String type, float value, CallbackInfoReturnable<Float> cir) {

        if (AttributeTypeRegistry.ATTR_TYPE_INC_PERK_EFFECT.equals(type)) {
            float max = (float) GctCoreConfig.astralSorceryIntegration.maxPerkEffect;
            if (max < 0) {
                return;
            }
            float current = cir.getReturnValueF();
            if (current > max) {
                cir.setReturnValue(max);
            }
        }
    }

    @Inject(
            method = "getModifier(Lnet/minecraft/entity/player/EntityPlayer;Lhellfirepvp/astralsorcery/common/data/research/PlayerProgress;Ljava/lang/String;Ljava/util/Collection;)F",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void onGetModifier(EntityPlayer player, PlayerProgress progress, String type, Collection<PerkAttributeModifier.Mode> modes, CallbackInfoReturnable<Float> cir) {
        if (AttributeTypeRegistry.ATTR_TYPE_INC_PERK_EFFECT.equals(type)) {
            float max = (float) GctCoreConfig.astralSorceryIntegration.maxPerkEffect;
            if (max < 0) {
                return;
            }
            float current = cir.getReturnValueF();
            if (current > max) {
                cir.setReturnValue(max);
            }
        }
    }
}