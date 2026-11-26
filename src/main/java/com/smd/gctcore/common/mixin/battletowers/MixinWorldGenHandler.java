package com.smd.gctcore.common.mixin.battletowers;

import com.smd.gctcore.common.config.GCTCompatConfig;
import com.smd.gctcore.common.integration.battletowers.BattleTowersDimensions;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

/**
 * Both the original mod and Battle Towers Fixes use this IWorldGenerator entry.
 * Command-driven creation/deletion bypasses it, so position-file I/O and all
 * manual tower operations keep their original behaviour.
 */
@Pseudo
@Mixin(targets = "atomicstryker.battletowers.common.WorldGenHandler", remap = false)
public abstract class MixinWorldGenHandler {
    @Inject(
            method = "generate(Ljava/util/Random;IILnet/minecraft/world/World;Lnet/minecraft/world/gen/IChunkGenerator;Lnet/minecraft/world/chunk/IChunkProvider;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            remap = false
    )
    private void gctcore$filterNaturalDimensions(Random random, int chunkX, int chunkZ, World world,
                                                IChunkGenerator chunkGenerator, IChunkProvider chunkProvider,
                                                CallbackInfo ci) {
        GCTCompatConfig.BattleTowers settings = GCTCompatConfig.battleTowers;
        if (settings.enableDimensionFilter && world != null && world.provider != null
                && !BattleTowersDimensions.allows(world.provider.getDimension(),
                settings.dimensionListIsWhitelist, settings.dimensionIds)) {
            ci.cancel();
        }
    }
}
