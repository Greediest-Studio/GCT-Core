package com.smd.gctcore.common.mixin.mmce;

import com.smd.gctcore.common.tile.blood_altar.BloodAltarMachine;
import hellfirepvp.modularmachinery.common.machine.factory.FactoryRecipeThread;
import hellfirepvp.modularmachinery.common.tiles.TileFactoryController;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Upgrades old persisted core-thread names before MMCE resolves its preset. */
@Mixin(value = FactoryRecipeThread.class, remap = false)
public abstract class MixinFactoryRecipeThread {

    @Inject(method = "deserialize", at = @At("HEAD"))
    private static void gctcore$upgradeBloodAltarThreadName(final NBTTagCompound tag,
                                                             final TileFactoryController factory,
                                                             final CallbackInfoReturnable<FactoryRecipeThread> callback) {
        if (!tag.hasKey("coreThreadName")) {
            return;
        }

        final String serializedName = tag.getString("coreThreadName");
        final String canonicalName = BloodAltarMachine.canonicalThreadName(serializedName);
        if (!serializedName.equals(canonicalName)) {
            tag.setString("coreThreadName", canonicalName);
        }
    }
}
