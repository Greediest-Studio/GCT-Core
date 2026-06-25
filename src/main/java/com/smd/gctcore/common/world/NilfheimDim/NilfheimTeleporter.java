package com.smd.gctcore.common.world.NilfheimDim;

import com.smd.gctcore.misc.BlockRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

public class NilfheimTeleporter extends Teleporter {
    private final WorldServer world;
    private final BlockPos target;

    public NilfheimTeleporter(WorldServer world, BlockPos target) {
        super(world);
        this.world = world;
        this.target = target;
    }

    @Override
    public void placeInPortal(Entity entity, float rotationYaw) {
        entity.setLocationAndAngles(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D, entity.rotationYaw, entity.rotationPitch);
        entity.motionX = 0.0D;
        entity.motionY = 0.0D;
        entity.motionZ = 0.0D;
    }

    @Override
    public boolean placeInExistingPortal(Entity entity, float rotationYaw) {
        placeInPortal(entity, rotationYaw);
        return true;
    }

    @Override
    public boolean makePortal(Entity entity) {
        return true;
    }

    @Override
    public void removeStalePortalLocations(long worldTime) {
    }

    public static BlockPos findSafeDestination(WorldServer world, double sourceX, double sourceZ) {
        int baseX = (int) Math.floor(sourceX);
        int baseZ = (int) Math.floor(sourceZ);
        int[] radii = {0, 4, 8, 16, 32};

        for (int radius : radii) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (radius != 0 && Math.abs(dx) != radius && Math.abs(dz) != radius) {
                        continue;
                    }

                    BlockPos found = findSafeColumnPosition(world, baseX + dx, baseZ + dz);
                    if (found != null) {
                        return found;
                    }
                }
            }
        }

        BlockPos fallback = new BlockPos(baseX, 80, baseZ);
        prepareFallbackPlatform(world, fallback);
        return fallback.up();
    }

    private static BlockPos findSafeColumnPosition(WorldServer world, int x, int z) {
        world.getChunk(new BlockPos(x, 64, z));
        for (int y = 180; y >= 32; y--) {
            BlockPos feet = new BlockPos(x, y, z);
            if (isSafeStandPosition(world, feet)) {
                return feet;
            }
        }
        return null;
    }

    private static boolean isSafeStandPosition(WorldServer world, BlockPos feet) {
        IBlockState below = world.getBlockState(feet.down());
        IBlockState feetState = world.getBlockState(feet);
        IBlockState headState = world.getBlockState(feet.up());
        Material belowMaterial = below.getMaterial();
        return below.isFullBlock()
                && belowMaterial.blocksMovement()
                && !belowMaterial.isLiquid()
                && below.getBlock() != Blocks.FIRE
                && feetState.getBlock().isAir(feetState, world, feet)
                && headState.getBlock().isAir(headState, world, feet.up());
    }

    private static void prepareFallbackPlatform(WorldServer world, BlockPos base) {
        IBlockState platform = BlockRegistry.RIMESTEEL_STONE.getDefaultState();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                world.setBlockState(base.add(dx, 0, dz), platform, 2);
            }
        }
        world.setBlockToAir(base.up());
        world.setBlockToAir(base.up(2));
    }
}
