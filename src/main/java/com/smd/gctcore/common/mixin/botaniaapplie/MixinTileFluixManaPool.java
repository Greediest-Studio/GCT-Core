package com.smd.gctcore.common.mixin.botaniaapplie;

import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "nyonio.tile.TileFluixManaPool", remap = false)
public abstract class MixinTileFluixManaPool {

    @Unique
    private static final int GCTCORE_LOCAL_MANA_CAPACITY = 1_000_000;

    @Shadow
    public int manaCap;

    @Inject(method = "<init>", at = @At("RETURN"), require = 1)
    private void gctcore$initializeLocalManaCapacity(CallbackInfo ci) {
        manaCap = GCTCORE_LOCAL_MANA_CAPACITY;
    }

    @ModifyConstant(
            method = { "func_73660_a", "update" },
            constant = @Constant(intValue = Integer.MAX_VALUE),
            require = 2
    )
    private int gctcore$limitLocalManaCapacity(int original) {
        return GCTCORE_LOCAL_MANA_CAPACITY;
    }

    @ModifyConstant(
            method = "readPacketNBT",
            constant = @Constant(intValue = Integer.MAX_VALUE, ordinal = 0),
            require = 1
    )
    private int gctcore$clampLoadedLocalMana(int original) {
        return GCTCORE_LOCAL_MANA_CAPACITY;
    }

    @Inject(method = "readPacketNBT", at = @At("RETURN"), require = 1)
    private void gctcore$restoreLocalManaCapacity(NBTTagCompound tag, CallbackInfo ci) {
        manaCap = GCTCORE_LOCAL_MANA_CAPACITY;
    }

    @ModifyConstant(
            method = "getAvailableSpaceForMana",
            constant = @Constant(intValue = Integer.MAX_VALUE),
            require = 1
    )
    private int gctcore$limitManaVoidInput(int original) {
        return GCTCORE_LOCAL_MANA_CAPACITY;
    }

    @ModifyConstant(
            method = "getTotalCapacity",
            constant = @Constant(longValue = Integer.MAX_VALUE),
            require = 2
    )
    private long gctcore$useLocalManaCapacityInTotal(long original) {
        return GCTCORE_LOCAL_MANA_CAPACITY;
    }
}
