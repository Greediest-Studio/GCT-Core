package com.smd.gctcore.misc.moretcon;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Interface that can be implemented by blocks in other mods
 * to make them compatible with MoreTcon's bedrock mining system.
 * 
 * This interface mirrors the IBedrockMineable interface from MoreTcon,
 * allowing blocks from other mods to be recognized as bedrock-like
 * without directly depending on MoreTcon.
 * 
 * To use this in your mod:
 * 1. Implement this interface in your block class
 * 2. Override isBedrockLike() to return true
 * 3. Optionally override isSoftBedrock() to control mining speed
 * 
 * Alternatively, you can add your blocks to Gct-Core's config whitelist
 * without modifying your mod's code.
 */
public interface IGctBedrockMineable {
    
    /**
     * Returns whether this block should be treated as bedrock-like
     * (requiring BottomsEnd trait to mine).
     * 
     * @param blockState The block state
     * @param worldIn The world
     * @param pos The block position
     * @return true if this block requires BottomsEnd to mine
     */
    default boolean isGctBedrockLike(IBlockState blockState, World worldIn, BlockPos pos) {
        return true;
    }
    
    /**
     * Returns whether this block should be treated as "soft bedrock".
     * Soft bedrock mines faster than regular bedrock when using BottomsEnd tools.
     * 
     * @param blockState The block state
     * @param worldIn The world
     * @param pos The block position
     * @return true if this should be soft bedrock (faster mining)
     */
    default boolean isGctSoftBedrock(IBlockState blockState, World worldIn, BlockPos pos) {
        return false;
    }
}
