package com.gmm.gctold.common.world.structure;

import com.gmm.gctold.common.blocks.BlockAlfStone;
import com.gmm.gctold.common.blocks.BlockAzathothiumOreComplex;
import com.gmm.gctold.common.blocks.BlockDenseDarkstone;
import com.gmm.gctold.common.blocks.BlockDreadiumOre;
import com.gmm.gctold.common.blocks.BlockElementiumOre;
import com.gmm.gctold.common.blocks.BlockEthauxiumOre;
import com.gmm.gctold.common.blocks.BlockManaStone;
import com.gmm.gctold.common.blocks.BlockManaStraw;
import com.gmm.gctold.common.blocks.BlockManaStrawSmall;
import com.gmm.gctold.common.blocks.BlockNyralathotepiumOreComplex;
import com.gmm.gctold.common.blocks.BlockOrderCrystalOre;
import com.gmm.gctold.common.blocks.BlockOrderLiquid;
import com.gmm.gctold.common.blocks.BlockOrderStone;
import com.gmm.gctold.common.blocks.BlockOrichalcosOre;
import com.gmm.gctold.common.blocks.BlockResonateDebris;
import com.gmm.gctold.common.blocks.BlockResonateDebrisCracked;
import com.gmm.gctold.common.blocks.BlockSaniteOre;
import com.gmm.gctold.common.blocks.BlockShubniggurathiumOreComplex;
import com.gmm.gctold.common.blocks.BlockTerrasteelOre;
import com.gmm.gctold.common.blocks.BlockWarprack;
import com.gmm.gctold.common.blocks.BlockWitheriumOreEnd;
import com.gmm.gctold.common.blocks.BlockWitheriumOreNether;
import com.gmm.gctold.common.blocks.BlockWitheriumOreOverworld;
import com.gmm.gctold.common.blocks.BlockYogsothothiumOreComplex;
import com.gmm.gctold.common.world.biome.BiomeDarkerRealm;
import com.gmm.gctold.common.world.biome.BiomeManaForest;
import com.gmm.gctold.common.world.biome.BiomeManaForestHill;
import com.gmm.gctold.common.world.biome.BiomeWarped;
import com.gmm.gctold.common.world.dimension.WorldAlfheim;
import com.gmm.gctold.common.world.dimension.WorldDarkerRealm;
import com.gmm.gctold.common.world.dimension.WorldOrderland;
import com.gmm.gctold.common.world.dimension.WorldWarpedRuin;
import com.google.common.base.Predicate;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenFlowers;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.feature.WorldGenMinable;

final class GctAllOreGenerator {
    private GctAllOreGenerator() {
    }

    static void generate(Random random, int chunkX, int chunkZ, World world, int dimensionId) {
        if (dimensionId == WorldOrderland.DIMID) {
            generateOrderLand(random, chunkX, chunkZ, world);
        } else if (dimensionId == WorldWarpedRuin.DIMID) {
            generateWarpedRuin(random, chunkX, chunkZ, world);
        } else if (dimensionId == WorldDarkerRealm.DIMID) {
            generateDarkerRealm(random, chunkX, chunkZ, world);
        } else if (dimensionId == WorldAlfheim.DIMID) {
            generateAlfheim(random, chunkX, chunkZ, world);
        } else if (dimensionId == 1) {
            generateEnd(random, chunkX, chunkZ, world);
            generateOre(random, chunkX, chunkZ, world, BlockWitheriumOreEnd.block, Blocks.END_STONE, 8, 4, 0, 255);
        } else if (dimensionId == -1) {
            generateOre(random, chunkX, chunkZ, world, BlockWitheriumOreNether.block, Blocks.NETHERRACK, 5, 3, 0, 127);
        } else if (dimensionId == 0) {
            generateOverworld(random, chunkX, chunkZ, world);
            generateOre(random, chunkX, chunkZ, world, BlockWitheriumOreOverworld.block, Blocks.STONE, 3, 3, 0, 10);
        }
    }

    private static void generateOrderLand(Random random, int chunkX, int chunkZ, World world) {
        int x = chunkX + random.nextInt(16);
        int y = random.nextInt(256);
        int z = chunkZ + random.nextInt(16);
        new WorldGenLakes(BlockOrderLiquid.block).generate(world, random, new BlockPos(x, y, z));
        generateOre(random, chunkX, chunkZ, world, BlockOrderCrystalOre.block, BlockOrderStone.block, 8, 1, 0, 128);
    }

    private static void generateWarpedRuin(Random random, int chunkX, int chunkZ, World world) {
        if (world.getBiome(new BlockPos(chunkX, 128, chunkZ)) != BiomeWarped.biome) {
            return;
        }
        generateOre(random, chunkX, chunkZ, world, BlockAzathothiumOreComplex.block, BlockWarprack.block, 3, 2, 0, 60);
        generateOre(random, chunkX, chunkZ, world, BlockNyralathotepiumOreComplex.block, BlockWarprack.block, 3, 2, 0, 60);
        generateOre(random, chunkX, chunkZ, world, BlockShubniggurathiumOreComplex.block, BlockWarprack.block, 3, 2, 0, 60);
        generateOre(random, chunkX, chunkZ, world, BlockYogsothothiumOreComplex.block, BlockWarprack.block, 3, 2, 0, 60);
    }

    private static void generateDarkerRealm(Random random, int chunkX, int chunkZ, World world) {
        if (world.getBiome(new BlockPos(chunkX, 128, chunkZ)) != BiomeDarkerRealm.biome) {
            return;
        }
        generateOre(random, chunkX, chunkZ, world, BlockDreadiumOre.block, BlockDenseDarkstone.block, 8, 5, 0, 128);
        generateOre(random, chunkX, chunkZ, world, BlockEthauxiumOre.block, BlockDenseDarkstone.block, 7, 3, 0, 128);
        generateOre(random, chunkX, chunkZ, world, BlockSaniteOre.block, BlockDenseDarkstone.block, 4, 2, 0, 128);
    }

    private static void generateAlfheim(Random random, int chunkX, int chunkZ, World world) {
        generateOre(random, chunkX, chunkZ, world, BlockElementiumOre.block, BlockAlfStone.block, 12, 6, 0, 64);
        generateOre(random, chunkX, chunkZ, world, BlockTerrasteelOre.block, BlockAlfStone.block, 2, 3, 0, 64);
        generateOre(random, chunkX, chunkZ, world, BlockOrichalcosOre.block, BlockAlfStone.block, 1, 12, 0, 64);
    }

    private static void generateEnd(Random random, int chunkX, int chunkZ, World world) {
        generateOre(random, chunkX, chunkZ, world, BlockResonateDebris.block, Blocks.END_STONE, 2, 1, 0, 80);
        generateOre(random, chunkX, chunkZ, world, BlockResonateDebrisCracked.block, Blocks.END_STONE, 2, 1, 0, 80);
    }

    private static void generateOverworld(Random random, int chunkX, int chunkZ, World world) {
        Biome biome = world.getBiome(new BlockPos(chunkX, 128, chunkZ));
        if (biome == BiomeManaForest.biome) {
            generateOre(random, chunkX, chunkZ, world, BlockManaStone.block, Blocks.STONE, 32, 32, 0, 64);
        }
        if (biome == BiomeManaForest.biome || biome == BiomeManaForestHill.biome) {
            generateFlowers(random, chunkX, chunkZ, world, BlockManaStraw.block, 20, 0, 128);
            generateFlowers(random, chunkX, chunkZ, world, BlockManaStrawSmall.block, 20, 0, 128);
        }
    }

    private static void generateOre(Random random, int chunkX, int chunkZ, World world, Block ore, Block target,
            int attempts, int veinSize, int minY, int maxY) {
        if (maxY <= minY) {
            return;
        }
        WorldGenMinable generator = new WorldGenMinable(ore.getDefaultState(), veinSize, new TargetBlockPredicate(target));
        int height = maxY - minY;
        for (int i = 0; i < attempts; i++) {
            int x = chunkX + random.nextInt(16);
            int y = minY + random.nextInt(height);
            int z = chunkZ + random.nextInt(16);
            generator.generate(world, random, new BlockPos(x, y, z));
        }
    }

    private static void generateFlowers(Random random, int chunkX, int chunkZ, World world, Block block, int attempts,
            int minY, int maxY) {
        if (!(block instanceof BlockFlower) || maxY <= minY) {
            return;
        }
        WorldGenFlowers generator = new WorldGenFlowers((BlockFlower) block, BlockFlower.EnumFlowerType.DANDELION);
        int height = maxY - minY;
        for (int i = 0; i < attempts; i++) {
            int x = chunkX + random.nextInt(16);
            int y = minY + random.nextInt(height);
            int z = chunkZ + random.nextInt(16);
            generator.generate(world, random, new BlockPos(x, y, z));
        }
    }

    private static final class TargetBlockPredicate implements Predicate<IBlockState> {
        private final Block target;

        private TargetBlockPredicate(Block target) {
            this.target = target;
        }

        @Override
        public boolean apply(IBlockState state) {
            return state.getBlock() == target;
        }
    }
}
