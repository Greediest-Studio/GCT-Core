package com.smd.gctcore.common.mixin.betternether;

import com.smd.gctcore.common.integration.betternether.BetterNetherCityCacheHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Defers BetterNether {@code BNWorldGenerator.cityManager} creation during Nether world load
 * and initializes it only when Nether chunk generation actually populates a chunk.
 */
@Pseudo
@Mixin(targets = "paulevs.betternether.world.BNWorldGenerator", remap = false)
public abstract class MixinBNWorldGenerator {

    @Inject(method = "init(Lnet/minecraft/world/World;)V", at = @At("HEAD"), remap = false)
    private static void gctcore$recordNetherWorld(World world, CallbackInfo ci) {
        BetterNetherCityCacheHelper.onNetherWorldLoad(world);
    }

    @Redirect(
            method = "init(Lnet/minecraft/world/World;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lpaulevs/betternether/config/ConfigLoader;hasCities()Z",
                    remap = false
            ),
            remap = false
    )
    private static boolean gctcore$skipEagerCityManagerInit() {
        return false;
    }

    @Inject(method = "smoothChunk(Lnet/minecraft/world/World;II)V", at = @At("HEAD"), remap = false)
    private static void gctcore$ensureCityManagerOnNetherGen(World world, int cx, int cz, CallbackInfo ci) {
        BetterNetherCityCacheHelper.ensureCityManagerForNether(world);
    }

    @Inject(method = "save(Lnet/minecraft/world/World;)V", at = @At("RETURN"), remap = false)
    private static void gctcore$demoteCityManagerWhenIdle(World world, CallbackInfo ci) {
        BetterNetherCityCacheHelper.onAfterWorldSave(world);
    }

    @Inject(
            method = "getNearestCity(Lnet/minecraft/world/World;II)Lnet/minecraft/util/math/BlockPos;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void gctcore$computeNearestCityIfUnloaded(World world, int cx, int cz, CallbackInfoReturnable<BlockPos> cir) {
        BlockPos fallback = BetterNetherCityCacheHelper.computeNearestCityIfUnloaded(world, cx, cz);
        if (fallback != null) {
            cir.setReturnValue(fallback);
        }
    }
}
