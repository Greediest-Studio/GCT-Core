package com.smd.gctcore.common.mixin.astralsorcery;

import hellfirepvp.astralsorcery.common.constellation.perk.tree.nodes.key.KeyCullAttack;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyCullAttack.class)
public class MixinKeyCullAttack {

    @Inject(method = "onDmg", at = @At("HEAD"), cancellable = true, remap = false)
    private void injectCullCheck(LivingDamageEvent event, CallbackInfo ci) {
        DamageSource source = event.getSource();
        if (source.getTrueSource() instanceof EntityPlayer) {
            EntityLivingBase attacked = event.getEntityLiving();
            if (!attacked.isNonBoss()) {

                ci.cancel();
            }
        }
    }
}







