package com.smd.gctcore.common.mixin.bloodmagic;

import WayofTime.bloodmagic.altar.BloodAltar;
import WayofTime.bloodmagic.tile.TileAltar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes Blood Magic's main altar tank without touching its I/O fluid tanks. */
@Mixin(TileAltar.class)
public interface TileAltarAccessor {

    @Accessor(value = "bloodAltar", remap = false)
    BloodAltar gctcore$getBloodAltar();
}
