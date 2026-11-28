package com.smd.gctcore.common.mixin.cyclopstek;

import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

/** Stops the registered world generator before template loading, placement or its boss spawn. */
@Pseudo
@Mixin(targets = "net.tangotek.cyclopstek.WorldGenCyclopsIsland", remap = false)
public abstract class MixinWorldGenCyclopsIsland {
    @Inject(
            method = "generate(Ljava/util/Random;IILnet/minecraft/world/World;Lnet/minecraft/world/gen/IChunkGenerator;Lnet/minecraft/world/chunk/IChunkProvider;)V",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            remap = false
    )
    private void gctcore$disableNaturalStructures(Random random, int chunkX, int chunkZ, World world,
                                                  IChunkGenerator generator, IChunkProvider provider,
                                                  CallbackInfo ci) {
        ci.cancel();
    }
}
