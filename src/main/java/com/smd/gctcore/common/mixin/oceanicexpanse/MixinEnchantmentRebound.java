package com.smd.gctcore.common.mixin.oceanicexpanse;

import com.sirsquidly.oe.enchantment.EnchantmentRebound;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EnchantmentRebound.class, remap = false)
public abstract class MixinEnchantmentRebound {

    /**
     * @reason Oceanic Expanse unconditionally executes `feet.setTagCompound(new NBTTagCompound())`
     * on every living entity tick before checking if the boots even have the Rebound enchantment.
     * This pollutes empty feet (ItemStack.EMPTY) and any boots equipped by entities with an empty `{}` NBT tag.
     */
    @Inject(
            method = "onLivingUpdate",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void gctcore$preventEmptyNbtInjection(LivingEvent.LivingUpdateEvent event, CallbackInfo ci) {
        if (event.getEntityLiving() == null || event.getEntityLiving().world.isRemote) {
            return;
        }
        ItemStack feet = event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.FEET);
        if (feet.isEmpty()) {
            ci.cancel();
            return;
        }
        Enchantment rebound = Enchantment.getEnchantmentByLocation("oe:rebound");
        if (rebound == null || EnchantmentHelper.getEnchantmentLevel(rebound, feet) <= 0) {
            ci.cancel();
        }
    }
}
