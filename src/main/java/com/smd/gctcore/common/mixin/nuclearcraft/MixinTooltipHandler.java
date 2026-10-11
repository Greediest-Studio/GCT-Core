package com.smd.gctcore.common.mixin.nuclearcraft;

import nc.handler.TooltipHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = TooltipHandler.class, remap = false)
public abstract class MixinTooltipHandler {

    /**
     * @reason Prevent NuclearCraft from attaching an empty NBT compound to items (especially armor/goggles) when querying tooltip.
     * NBTHelper.getStackNBT mutates the stack by creating a new empty NBTCompound if null.
     */
    @Redirect(
            method = "addArmorRadiationTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Lnc/util/NBTHelper;getStackNBT(Lnet/minecraft/item/ItemStack;[Ljava/lang/String;)Lnet/minecraft/nbt/NBTTagCompound;"
            ),
            remap = false
    )
    private static NBTTagCompound gctcore$safeGetArmorNBT(ItemStack stack, String... subTags) {
        return stack.hasTagCompound() ? stack.getTagCompound() : null;
    }
}
