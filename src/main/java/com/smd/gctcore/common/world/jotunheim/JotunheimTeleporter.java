package com.smd.gctcore.common.world.jotunheim;

import com.smd.gctcore.common.config.JotunheimConfig;
import com.smd.gctcore.misc.JotunheimBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.gen.ChunkProviderServer;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/** Chooses solid island ground in the selected ward's stratum, rather than the world's highest surface. */
public final class JotunheimTeleporter extends Teleporter {
    private static final int MAX_CANDIDATE_CHUNKS = 12;
    private static final int SEARCH_CHUNK_RADIUS = 48;
    private final BlockPos target;

    public JotunheimTeleporter(WorldServer world, BlockPos target) {
        super(world);
        this.target = target;
    }

    @Override
    public void placeInPortal(Entity entity, float rotationYaw) {
        entity.setLocationAndAngles(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D,
                entity.rotationYaw, entity.rotationPitch);
        entity.motionX = 0.0D;
        entity.motionY = 0.0D;
        entity.motionZ = 0.0D;
        entity.fallDistance = 0.0F;
    }

    @Override
    public boolean placeInExistingPortal(Entity entity, float rotationYaw) {
        placeInPortal(entity, rotationYaw);
        return true;
    }

    @Override public boolean makePortal(Entity entity) { return true; }
    @Override public void removeStalePortalLocations(long worldTime) { }

    public static BlockPos findSafeDestination(WorldServer world, double sourceX, double sourceZ, int layerIndex) {
        JotunheimTerrainKernel.HeightLayer layer = JotunheimTerrainKernel.HeightLayer.values()[
                layerIndex >= 0 && layerIndex < 4 ? layerIndex : 3];
        WorldBorder border = world.getWorldBorder();
        int baseX = MathHelper.floor(MathHelper.clamp(sourceX, border.minX() + 3, border.maxX() - 3));
        int baseZ = MathHelper.floor(MathHelper.clamp(sourceZ, border.minZ() + 3, border.maxZ() - 3));
        ChunkProviderServer provider = world.getChunkProvider();
        JotunheimTerrainKernel kernel = provider != null && provider.chunkGenerator instanceof ChunkGeneratorJotunheim
                ? ((ChunkGeneratorJotunheim) provider.chunkGenerator).terrainKernel()
                : new JotunheimTerrainKernel(world.getSeed(), JotunheimConfig.snapshot());
        Set<Long> visited = new HashSet<>();
        BlockPos fallback = null;
        int attempts = 0;
        int originChunkX = baseX >> 4;
        int originChunkZ = baseZ >> 4;

        // Terrain-only predictions skip void without generating every intervening chunk.
        search:
        for (int ring = 0; ring <= SEARCH_CHUNK_RADIUS; ring++) {
            for (int dx = -ring; dx <= ring; dx++) {
                for (int dz = -ring; dz <= ring; dz++) {
                    if (ring != 0 && Math.abs(dx) != ring && Math.abs(dz) != ring) continue;
                    int x = ring == 0 ? baseX : ((originChunkX + dx) << 4) + 8;
                    int z = ring == 0 ? baseZ : ((originChunkZ + dz) << 4) + 8;
                    if (!insideBorder(border, x, z)) continue;
                    int predicted = predictedFeet(kernel, x, z, layer);
                    if (predicted < 0) continue;
                    long key = ((long) (x >> 4) << 32) ^ ((z >> 4) & 0xFFFFFFFFL);
                    if (!visited.add(key)) continue;
                    if (fallback == null) fallback = new BlockPos(
                            ((x >> 4) << 4) + MathHelper.clamp(x & 15, 1, 14), predicted,
                            ((z >> 4) << 4) + MathHelper.clamp(z & 15, 1, 14));
                    world.getChunk(new BlockPos(x, predicted, z));
                    BlockPos safe = findSafeInChunk(world, x >> 4, z >> 4, x, z, layer);
                    if (safe != null) return safe;
                    if (++attempts >= MAX_CANDIDATE_CHUNKS) break search;
                }
            }
        }

        // A saved island may have been mined away or every landing surface built over.
        // Keep this last resort in the requested layer, with real solid footing and clear headroom.
        if (fallback == null) fallback = new BlockPos(baseX, layer.typicalSurface + 1, baseZ);
        prepareLanding(world, fallback, layer);
        return fallback;
    }

    private static int predictedFeet(JotunheimTerrainKernel kernel, int x, int z,
                                     JotunheimTerrainKernel.HeightLayer layer) {
        int[] surfaces = kernel.surfaceHeights(x, z);
        for (int i = surfaces.length - 1; i >= 0; i--) {
            int y = surfaces[i];
            if (y > layer.minY && y <= layer.maxY && YmirForceRules.layerAtHeight(y) == layer.ordinal()) return y;
        }
        return -1;
    }

    @Nullable
    private static BlockPos findSafeInChunk(WorldServer world, int chunkX, int chunkZ, int sourceX, int sourceZ,
                                            JotunheimTerrainKernel.HeightLayer layer) {
        BlockPos best = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        // The one-block inset keeps all 3x3 safety checks inside this single loaded chunk.
        for (int x = (chunkX << 4) + 1; x <= (chunkX << 4) + 14; x++) {
            for (int z = (chunkZ << 4) + 1; z <= (chunkZ << 4) + 14; z++) {
                if (!insideBorder(world.getWorldBorder(), x, z)) continue;
                double distance = (x - sourceX) * (x - sourceX) + (z - sourceZ) * (z - sourceZ);
                if (distance >= bestDistance) continue;
                for (int y = layer.maxY; y > layer.minY; y--) {
                    BlockPos feet = new BlockPos(x, y, z);
                    if (isSafeLanding(world, feet, layer)) {
                        best = feet;
                        bestDistance = distance;
                        break;
                    }
                }
            }
        }
        return best;
    }

    private static boolean isSafeLanding(WorldServer world, BlockPos feet,
                                          JotunheimTerrainKernel.HeightLayer layer) {
        if (feet.getY() <= layer.minY || feet.getY() > layer.maxY
                || YmirForceRules.layerAtHeight(feet.getY()) != layer.ordinal()) return false;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos cell = feet.add(dx, 0, dz);
                if (!isLand(world.getBlockState(cell.down()))) return false;
                for (int dy = 0; dy <= 2; dy++) {
                    BlockPos body = cell.up(dy);
                    IBlockState state = world.getBlockState(body);
                    if (!state.getBlock().isAir(state, world, body)) return false;
                }
            }
        }
        return true;
    }

    private static boolean isLand(IBlockState state) {
        Block block = state.getBlock();
        Material material = state.getMaterial();
        if (!state.isFullCube() || !material.blocksMovement() || material.isLiquid()
                || block == Blocks.MAGMA || block == Blocks.CACTUS || block == Blocks.FIRE
                || block == JotunheimBlocks.get("heated_rock")) return false;
        // A floating cloud, canopy, log or crystal cluster is not island land.
        return material == Material.ROCK || material == Material.GROUND || material == Material.GRASS
                || material == Material.SAND || material == Material.ICE || material == Material.SNOW
                || material == Material.CRAFTED_SNOW;
    }

    private static boolean insideBorder(WorldBorder border, int x, int z) {
        return x - 1 >= border.minX() && x + 2 <= border.maxX()
                && z - 1 >= border.minZ() && z + 2 <= border.maxZ();
    }

    private static void prepareLanding(WorldServer world, BlockPos feet,
                                        JotunheimTerrainKernel.HeightLayer layer) {
        IBlockState ground = JotunheimBlocks.state(layer == JotunheimTerrainKernel.HeightLayer.CLOUD
                ? "cloudrock" : "giantstone");
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos cell = feet.add(dx, 0, dz);
                world.setBlockState(cell.down(), ground, 2);
                for (int dy = 0; dy <= 2; dy++) world.setBlockState(cell.up(dy), Blocks.AIR.getDefaultState(), 2);
            }
        }
    }
}
