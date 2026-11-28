package com.smd.gctcore.common.mixin.vanilla.world;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevents {@link WorldEntitySpawner#canCreatureTypeSpawnAtLocation} from synchronously loading
 * unloaded edge chunks when spawn pack offsets step past the loaded chunk radius.
 */
@Mixin(WorldEntitySpawner.class)
public abstract class MixinWorldEntitySpawner {

    @Inject(method = "canCreatureTypeSpawnAtLocation", at = @At("HEAD"), cancellable = true)
    private static void gctcore$skipUnloadedChunkSpawnCheck(
            EntityLiving.SpawnPlacementType spawnPlacementTypeIn,
            World worldIn,
            BlockPos pos,
            CallbackInfoReturnable<Boolean> cir) {
        if (worldIn != null && pos != null && !worldIn.isBlockLoaded(pos)) {
            cir.setReturnValue(false);
        }
    }
}
