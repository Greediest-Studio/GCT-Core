
package com.gmm.gctold.common.events;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
public final class SenterianSummonerSmallSummon
{
    private SenterianSummonerSmallSummon() {
    }
    
    public static void run(World world, int x, int y, int z) {
        if (world.isRemote) {
            return;
        }
        world.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState(), 3);
        Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation("journey:guardianofdestruction"), world);
        if (entity != null) {
            entity.setLocationAndAngles(x + 0.5D, y, z + 0.5D, world.rand.nextFloat() * 360.0F, 0.0F);
            world.spawnEntity(entity);
        }
    }
}


