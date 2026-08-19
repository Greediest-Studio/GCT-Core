package com.smd.gctcore.common.mixin.botania;

import com.smd.gctcore.common.blocks.botania.BlockGctManaPool;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import vazkii.botania.client.render.tile.RenderTilePool;
import vazkii.botania.common.block.ModBlocks;

@Mixin(value = RenderTilePool.class, remap = true)
public abstract class MixinRenderTilePool {

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/state/IBlockState;getBlock()Lnet/minecraft/block/Block;"
            )
    )
    private Block gctcore$renderCustomManaPools(IBlockState state) {
        Block block = state.getBlock();
        return block instanceof BlockGctManaPool ? ModBlocks.pool : block;
    }
}
