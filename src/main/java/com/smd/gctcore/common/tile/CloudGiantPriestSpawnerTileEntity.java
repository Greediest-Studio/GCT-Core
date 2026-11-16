package com.smd.gctcore.common.tile;

import com.smd.gctcore.common.entity.EntityCloudGiantCrystal;
import com.smd.gctcore.common.entity.EntityCloudGiantPriest;
import com.smd.gctcore.misc.BlockRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.World;

public class CloudGiantPriestSpawnerTileEntity extends TileEntity implements ITickable {

    @Override
    public void update() {
        if (world == null || world.isRemote || isInvalid()) {
            return;
        }
        final BlockPos origin = pos;
        final double centerX = origin.getX() + 0.5D;
        final double centerZ = origin.getZ() + 0.5D;
        for (EntityPlayer player : world.playerEntities) {
            if (!(player instanceof EntityPlayerMP)
                    || ((EntityPlayerMP) player).interactionManager.getGameType() != GameType.SURVIVAL) {
                continue;
            }
            double dx = player.posX - centerX;
            double dy = player.posY - origin.getY();
            double dz = player.posZ - centerZ;
            if (dx * dx + dy * dy + dz * dz <= 225.0D) {
                spawnEncounter(origin, centerX, centerZ);
                return;
            }
        }
    }

    private void spawnEncounter(BlockPos origin, double centerX, double centerZ) {
        World currentWorld = world;
        currentWorld.setBlockToAir(origin);
        removeDarkHighcloudStone(origin);

        EntityCloudGiantPriest boss = new EntityCloudGiantPriest(currentWorld);
        boss.setArenaOrigin(origin);
        boss.setLocationAndAngles(centerX, origin.getY(), centerZ,
                currentWorld.rand.nextFloat() * 360.0F, 0.0F);
        if (!currentWorld.spawnEntity(boss)) {
            return;
        }

        int[][] offsets = {{61, 0}, {-61, 0}, {0, 61}, {0, -61}};
        for (int[] offset : offsets) {
            EntityCloudGiantCrystal crystal = new EntityCloudGiantCrystal(currentWorld);
            crystal.setBossUuid(boss.getUniqueID());
            crystal.setLocationAndAngles(centerX + offset[0], origin.getY(), centerZ + offset[1], 0.0F, 0.0F);
            currentWorld.getChunk(new BlockPos(crystal.posX, crystal.posY, crystal.posZ));
            if (currentWorld.spawnEntity(crystal)) {
                boss.registerCrystal(crystal.getUniqueID());
            }
        }
    }

    private void removeDarkHighcloudStone(BlockPos origin) {
        for (BlockPos target : BlockPos.getAllInBox(origin.add(-8, -8, -8), origin.add(8, 8, 8))) {
            if (target.distanceSq(origin) <= 64.0D
                    && world.getBlockState(target).getBlock() == BlockRegistry.DARK_HIGHCLOUD_STONE) {
                world.setBlockToAir(target);
            }
        }
    }
}
