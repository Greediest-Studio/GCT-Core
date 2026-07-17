package com.smd.gctcore.common.mixin.mekanism;

import com.smd.gctcore.common.config.GCTCompatConfig;
import com.smd.gctcore.common.integration.mekanism.MekanismHarvestHelper;
import mekanism.common.tile.laser.TileEntityLaser;
import mekanism.common.tile.laser.TileEntityLaserAmplifier;
import mekanism.common.tile.laser.TileEntityLaserTractorBeam;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Stops laser dig progress on blocks above the harvest-level cap by treating them as unbreakable.
 */
@Mixin(value = {
        TileEntityLaser.class,
        TileEntityLaserAmplifier.class,
        TileEntityLaserTractorBeam.class
}, remap = false)
public class MixinLaserDigging {

    @Redirect(
            method = {"onUpdateServer", "onUpdateClient"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/state/IBlockState;getBlockHardness(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)F",
                    remap = true
            )
    )
    private float gct$restrictLaserHardness(IBlockState state, World world, BlockPos pos) {
        float hardness = state.getBlockHardness(world, pos);
        if (!GCTCompatConfig.mekanismIntegration.enableLaserHarvestLimit) {
            return hardness;
        }
        if (hardness < 0.0F) {
            return hardness;
        }
        if (!MekanismHarvestHelper.canLaserMine(state)) {
            return -1.0F;
        }
        return hardness;
    }
}
