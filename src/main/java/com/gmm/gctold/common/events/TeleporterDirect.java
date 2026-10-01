package com.gmm.gctold.common.events;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

public class TeleporterDirect extends Teleporter {
  private static final int SEARCH_RADIUS = 32;

  public TeleporterDirect(WorldServer world) {
    super(world);
  }

  @Override
  public void placeInPortal(Entity entity, float rotationYaw) {
    BlockPos pos = findSafeTeleportPos(this.world, new BlockPos(entity));
    entity.setLocationAndAngles(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, entity.rotationYaw, entity.rotationPitch);
    entity.motionX = 0.0D;
    entity.motionY = 0.0D;
    entity.motionZ = 0.0D;
  }

  @Override
  public boolean placeInExistingPortal(Entity entity, float rotationYaw) {
    return true;
  }

  @Override
  public boolean makePortal(Entity entity) {
    return true;
  }

  public static boolean transferToSpawn(EntityPlayerMP player, int dimension) {
    WorldServer targetWorld = getOrLoadWorld(player, dimension);
    if (targetWorld == null) {
      return false;
    }
    return transferToDimension(player, dimension, targetWorld.getSpawnPoint());
  }

  public static boolean transferToDimension(EntityPlayerMP player, int dimension, BlockPos target) {
    WorldServer targetWorld = player.server.getWorld(dimension);
    if (targetWorld == null) {
      DimensionManager.initDimension(dimension);
      targetWorld = DimensionManager.getWorld(dimension);
    }
    if (targetWorld == null) {
      return false;
    }
    BlockPos safePos = findSafeTeleportPos(targetWorld, target);
    player.changeDimension(dimension, new TeleporterDirect(targetWorld));
    if (player.dimension != dimension) {
      return false;
    }
    player.connection.setPlayerLocation(safePos.getX() + 0.5D, safePos.getY(), safePos.getZ() + 0.5D, player.rotationYaw, player.rotationPitch);
    player.motionX = 0.0D;
    player.motionY = 0.0D;
    player.motionZ = 0.0D;
    player.timeUntilPortal = player.getPortalCooldown();
    return true;
  }

  public static BlockPos findSafeTeleportPos(WorldServer world, BlockPos preferred) {
    BlockPos clamped = clampToWorld(world, preferred);
    BlockPos found = searchSafePos(world, clamped, SEARCH_RADIUS);
    if (found != null) {
      return found;
    }
    BlockPos top = world.getTopSolidOrLiquidBlock(new BlockPos(clamped.getX(), 0, clamped.getZ()));
    found = searchSafePos(world, clampToWorld(world, top), SEARCH_RADIUS);
    if (found != null) {
      return found;
    }
    return createSafePlatform(world, clamped);
  }

  private static WorldServer getOrLoadWorld(EntityPlayerMP player, int dimension) {
    WorldServer targetWorld = player.server.getWorld(dimension);
    if (targetWorld == null) {
      DimensionManager.initDimension(dimension);
      targetWorld = DimensionManager.getWorld(dimension);
    }
    return targetWorld;
  }

  private static BlockPos searchSafePos(WorldServer world, BlockPos center, int radius) {
    for (int r = 0; r <= radius; r++) {
      for (int dx = -r; dx <= r; dx++) {
        for (int dz = -r; dz <= r; dz++) {
          if (Math.abs(dx) != r && Math.abs(dz) != r) {
            continue;
          }
          int x = center.getX() + dx;
          int z = center.getZ() + dz;
          int maxY = Math.min(world.getActualHeight() - 2, center.getY() + radius);
          int minY = Math.max(1, center.getY() - radius);
          for (int y = maxY; y >= minY; y--) {
            BlockPos candidate = new BlockPos(x, y, z);
            if (isSafeTeleportPos(world, candidate)) {
              return candidate;
            }
          }
          for (int y = world.getActualHeight() - 2; y >= 1; y--) {
            BlockPos candidate = new BlockPos(x, y, z);
            if (isSafeTeleportPos(world, candidate)) {
              return candidate;
            }
          }
        }
      }
    }
    return null;
  }

  public static boolean isSafeTeleportPos(World world, BlockPos feet) {
    IBlockState floor = world.getBlockState(feet.down());
    return isFullSolidFloor(floor) && isEmptyForPlayer(world, feet) && isEmptyForPlayer(world, feet.up());
  }

  private static boolean isFullSolidFloor(IBlockState state) {
    return state.isFullCube() && state.getMaterial().blocksMovement() && !state.getMaterial().isLiquid();
  }

  private static boolean isEmptyForPlayer(World world, BlockPos pos) {
    IBlockState state = world.getBlockState(pos);
    return state.getBlock().isAir(state, world, pos);
  }

  private static BlockPos createSafePlatform(WorldServer world, BlockPos preferred) {
    BlockPos feet = clampToWorld(world, preferred);
    world.setBlockState(feet.down(), Blocks.STONE.getDefaultState(), 3);
    world.setBlockToAir(feet);
    world.setBlockToAir(feet.up());
    return feet;
  }

  private static BlockPos clampToWorld(World world, BlockPos pos) {
    int y = Math.max(1, Math.min(world.getActualHeight() - 2, pos.getY()));
    return new BlockPos(pos.getX(), y, pos.getZ());
  }
}
