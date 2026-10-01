
package com.gmm.gctold.common.events;

import com.gmm.gctold.common.blocks.BlockSenterianLament;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumHand;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class WrenchBreak
{
    private static final ResourceLocation JOURNEY_LAMENT = new ResourceLocation("journey", "lament");

    private WrenchBreak() {
    }
    
    public static void run(Entity entity, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        Block journeyLament = ForgeRegistries.BLOCKS.getValue(JOURNEY_LAMENT);
        IBlockState state = world.getBlockState(pos);
        if (journeyLament != null && state.getBlock() == journeyLament) {
            world.setBlockState(pos, BlockSenterianLament.block.getDefaultState(), 3);
        }
        if (entity instanceof EntityLivingBase) {
            ((EntityLivingBase)entity).swingArm(EnumHand.MAIN_HAND);
        }
    }
}


