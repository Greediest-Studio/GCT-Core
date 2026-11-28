package com.smd.gctcore.common.mixin.netherex;

import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

/**
 * Caches dimensions known to have no {@code PigtificateVillageData} on disk.
 *
 * <p>NetherEx's {@code PigtificateVillageManager#onWorldTick} calls
 * {@code getVillageData(world, false)} every server tick for every loaded dimension,
 * but {@code getVillageData} only caches non-null results in {@code pigtificateVillageData}.
 * Dimensions without a saved {@code pigtificate_villages.dat} file therefore trigger
 * synchronous filesystem {@code File.exists()} checks 20 times per second per dimension.</p>
 */
@Pseudo
@Mixin(targets = "logictechcorp.netherex.village.PigtificateVillageManager", remap = false)
public abstract class MixinPigtificateVillageManager {

    @Unique
    private final Set<Integer> gctcore$missingDimensions = new HashSet<>();

    @Inject(
            method = "getVillageData(Lnet/minecraft/world/World;Z)Llogictechcorp/netherex/village/PigtificateVillageData;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void gctcore$skipMissingDimensionLookup(World world, boolean create, CallbackInfoReturnable<?> cir) {
        if (!create && world != null && world.provider != null
                && this.gctcore$missingDimensions.contains(world.provider.getDimension())) {
            cir.setReturnValue(null);
        }
    }

    @Inject(
            method = "getVillageData(Lnet/minecraft/world/World;Z)Llogictechcorp/netherex/village/PigtificateVillageData;",
            at = @At("RETURN"),
            remap = false
    )
    private void gctcore$cacheMissingDimensionLookup(World world, boolean create, CallbackInfoReturnable<?> cir) {
        if (world == null || world.provider == null) {
            return;
        }
        int dimension = world.provider.getDimension();
        if (cir.getReturnValue() != null) {
            this.gctcore$missingDimensions.remove(dimension);
        } else if (!create) {
            this.gctcore$missingDimensions.add(dimension);
        }
    }

    @Inject(
            method = "unloadVillageData(I)V",
            at = @At("TAIL"),
            remap = false
    )
    private void gctcore$clearMissingDimensionOnUnload(int dimension, CallbackInfo ci) {
        this.gctcore$missingDimensions.remove(dimension);
    }

    @Inject(
            method = "onWorldUnload(Lnet/minecraftforge/event/world/WorldEvent$Unload;)V",
            at = @At("TAIL"),
            remap = false
    )
    private void gctcore$clearAllMissingDimensionsOnOverworldUnload(WorldEvent.Unload event, CallbackInfo ci) {
        if (event == null) {
            return;
        }
        World world = event.getWorld();
        if (world != null && !world.isRemote && world.provider != null
                && world.provider.getDimension() == DimensionType.OVERWORLD.getId()) {
            this.gctcore$missingDimensions.clear();
        }
    }
}
