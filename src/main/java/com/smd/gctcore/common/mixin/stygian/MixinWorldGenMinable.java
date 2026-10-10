package com.smd.gctcore.common.mixin.stygian;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenMinable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Stygian decorates its lakes and caves before the biome ore pass.  A few ore
 * generators consider air or fluids replaceable and consequently fill those
 * spaces when the late ore pass runs.  Keep the normal ore behaviour everywhere
 * else, but never write an ore into an empty or liquid block in Stygian's world.
 */
@Mixin(WorldGenMinable.class)
public abstract class MixinWorldGenMinable {
    private static final String STYGIAN_PROVIDER = "fluke.stygian.world.WorldProviderEndBiomes";

    @Redirect(
            method = "generate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;I)Z"
            )
    )
    private boolean gctcore$skipStygianCavityOre(
            World world, BlockPos pos, IBlockState oreState, int flags
    ) {
        if (world.provider.getClass().getName().equals(STYGIAN_PROVIDER)) {
            IBlockState current = world.getBlockState(pos);
            if (current.getBlock().isAir(current, world, pos) || current.getMaterial().isLiquid()) {
                return false;
            }
        }

        return world.setBlockState(pos, oreState, flags);
    }
}
