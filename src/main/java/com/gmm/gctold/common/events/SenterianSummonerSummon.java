
package com.gmm.gctold.common.events;

import com.gmm.gctold.common.blocks.BlockSenterianLament;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
public final class SenterianSummonerSummon
{
    private SenterianSummonerSummon() {
    }
    
    public static boolean run(World world, int x, int y, int z) {
        if (world.isRemote) {
            return false;
        }

        BlockPos pos = new BlockPos(x, y, z);
        TileEntity tile = world.getTileEntity(pos);
        if (tile != null && tile.getTileData().getBoolean("summoned")) {
            return true;
        }

        if (world.getBlockState(new BlockPos(x + 12, y + 0, z + 0)).getBlock() == BlockSenterianLament.block.getDefaultState().getBlock() && world.getBlockState(new BlockPos(x + 0, y + 0, z + 12)).getBlock() == BlockSenterianLament.block.getDefaultState().getBlock() && world.getBlockState(new BlockPos(x - 12, y + 0, z + 0)).getBlock() == BlockSenterianLament.block.getDefaultState().getBlock() && world.getBlockState(new BlockPos(x + 0, y + 0, z - 12)).getBlock() == BlockSenterianLament.block.getDefaultState().getBlock() && world.getBlockState(new BlockPos(x + 10, y + 0, z + 10)).getBlock() == BlockSenterianLament.block.getDefaultState().getBlock() && world.getBlockState(new BlockPos(x - 10, y + 0, z + 10)).getBlock() == BlockSenterianLament.block.getDefaultState().getBlock() && world.getBlockState(new BlockPos(x - 10, y + 0, z - 10)).getBlock() == BlockSenterianLament.block.getDefaultState().getBlock() && world.getBlockState(new BlockPos(x + 10, y + 0, z - 10)).getBlock() == BlockSenterianLament.block.getDefaultState().getBlock()) {
            if (tile != null) {
                tile.getTileData().setBoolean("summoned", true);
                tile.markDirty();
            }
            clearSummonArea(world, pos);
            spawnEntity(world, pos, "journey:sentryheart");
            tell(world, pos, "哨兵之心 已苏醒！");
            return true;
        }
        return false;
    }

    private static void clearSummonArea(World world, BlockPos center) {
        for (int dx = -5; dx <= 5; dx++) {
            for (int dy = -1; dy <= 17; dy++) {
                for (int dz = -5; dz <= 5; dz++) {
                    BlockPos pos = center.add(dx, dy, dz);
                    if (world.getBlockState(pos).getBlock() != Blocks.AIR) {
                        world.setBlockToAir(pos);
                    }
                }
            }
        }
    }

    private static void spawnEntity(World world, BlockPos pos, String id) {
        Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(id), world);
        if (entity != null) {
            entity.setLocationAndAngles(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, world.rand.nextFloat() * 360.0F, 0.0F);
            world.spawnEntity(entity);
        }
    }

    private static void tell(World world, BlockPos pos, String message) {
        TextComponentString component = new TextComponentString(message);
        AxisAlignedBB area = new AxisAlignedBB(pos).grow(32.0D);
        for (EntityPlayer player : world.getEntitiesWithinAABB(EntityPlayer.class, area,
                player -> player.getDistanceSq(pos) <= 1024.0D)) {
            player.sendMessage(component);
        }
    }
}


