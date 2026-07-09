package com.smd.gctcore.common.mixin.tconevo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import slimeknights.tconstruct.library.tools.ranged.ProjectileCore;
import slimeknights.tconstruct.tools.traits.TraitEnderference;
import xyz.phanta.tconevo.item.tool.ItemToolSceptre;

@Mixin(value = ItemToolSceptre.class, remap = false)
public abstract class MixinItemToolSceptre {

    @Redirect(
            method = "dealDamageRanged",
            at = @At(
                    value = "INVOKE",
                    target = "Lxyz/phanta/tconevo/util/DamageUtils;getProjectileDamageSource(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/DamageSource;"
            ),
            remap = false
    )
    private DamageSource tconevo$removeProjectileFlag(
            Entity target,
            EntityLivingBase attacker,
            Entity projectile
    ) {
        if (target instanceof EntityEnderman
                && ((EntityEnderman) target).getActivePotionEffect(TraitEnderference.Enderference) != null) {
            return new ProjectileCore.DamageSourceProjectileForEndermen("magic", projectile, attacker);
        }

        return new EntityDamageSourceIndirect("magic", projectile, attacker);
    }
}