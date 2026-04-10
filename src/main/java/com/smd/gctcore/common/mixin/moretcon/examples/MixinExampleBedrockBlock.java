package com.smd.gctcore.common.mixin.moretcon.examples;

import com.smd.gctcore.common.misc.moretcon.IGctBedrockMineable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.block.BlockDirt;

/**
 * EXAMPLE MIXIN - NOT ACTIVE BY DEFAULT
 * 
 * This is an example of how to inject the IGctBedrockMineable interface
 * into a block from another mod using Mixin.
 * 
 * To use this pattern:
 * 1. Copy this file and modify the @Mixin target to your desired block class
 * 2. Implement isGctBedrockLike() and isGctSoftBedrock() as needed
 * 3. Add the mixin class name to mixins.gctcore.moretcon.json
 * 
 * Example targets:
 *   - @Mixin(value = BlockObsidian.class)  // Vanilla obsidian
 *   - @Mixin(value = MyCustomBlock.class, remap = false)  // Custom mod block
 * 
 * Note: This example targets BlockDirt, which you should change to your actual target.
 */
@Mixin(value = BlockDirt.class, remap = true)
public abstract class MixinExampleBedrockBlock implements IGctBedrockMineable {

    /**
     * Make this block require BottomsEnd trait to mine.
     * Return true to enable, false to disable.
     */
    @Override
    public boolean isGctBedrockLike(IBlockState blockState, World worldIn, BlockPos pos) {
        // Example: only certain metadata values are bedrock-like
        // return blockState.getValue(SOME_PROPERTY) == SOME_VALUE;
        
        // Simple case: all instances are bedrock-like
        return true;
    }

    /**
     * Control mining speed: soft bedrock mines faster than regular bedrock.
     * Return true for faster mining, false for slower mining.
     */
    @Override
    public boolean isGctSoftBedrock(IBlockState blockState, World worldIn, BlockPos pos) {
        // Example: treat as soft bedrock for faster mining
        return true;
    }
}
