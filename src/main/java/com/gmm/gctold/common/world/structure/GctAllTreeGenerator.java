package com.gmm.gctold.common.world.structure;

import com.gmm.gctold.common.world.biome.WorldGenAlfheimTree;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class GctAllTreeGenerator {
    private static final String MOD_ID = "gctcore";

    private static final TreeDefinition[] TREES = {
            tree(41, "bloodyplain", "evileye_log", "evileye_leaves", 6, 4, 5, 1, "bloodygrass"),
            tree(41, "curruptplain", "evileye_log", "evileye_leaves", 6, 4, 5, 1, "corruptgrass"),
            tree(0, "mana_forest", "livingwood_log", "livingwood_leaves", 4, 1, 1, 6, "mana_grass"),
            tree(0, "mana_forest", "mana_log", "mana_leaves", 8, 6, 1, 2, "mana_grass"),
            tree(0, "mana_forest_hill", "livingwood_log", "livingwood_leaves", 4, 1, 1, 6, "mana_grass"),
            tree(0, "mana_forest_hill", "mana_log", "mana_leaves", 8, 6, 1, 2, "mana_grass"),
            tree(42, "alfheim_forest", "dreamwood_log", "dreamwood_leaves", 4, 1, 1, 2, "alf_grass"),
            tree(42, "reversed_forest", "reversed_dreamwood_log", "reversed_dreamwood_leaves", 4, 1, 1, 2,
                    "reversed_alf_grass"),
            tree(80, "aries_forest", external("blue_skies", "starlit_log"),
                    external("blue_skies", "starlit_leaves"), 8, 8, 2, 2, "star_grass"),
            tree(80, "sagittarius_denseforest", external("blue_skies", "starlit_log"),
                    external("blue_skies", "starlit_leaves"), 8, 4, 1, 2, "star_grass"),
            tree(80, "taurus_plataeu", external("blue_skies", "starlit_log"),
                    external("blue_skies", "starlit_leaves"), 4, 4, 4, 2, "star_grass"),
            tree(80, "cancer_hills", external("astralsorcery", "blockinfusedwood"), local("wildplain_leaves"), 10,
                    6, 1, 2, "lunar_grass"),
            tree(80, "virgo_plain", external("astralsorcery", "blockinfusedwood"), local("wildplain_leaves"), 10,
                    6, 6, 2, "lunar_grass"),
            tree(80, "gemini_valley", external("astralsorcery", "blockinfusedwood"), local("wildplain_leaves"), 10,
                    6, 1, 2, "lunar_grass")
    };

    private GctAllTreeGenerator() {
    }

    public static void generate(Random random, int blockX, int blockZ, World world, int dimensionId) {
        if (world.isRemote) {
            return;
        }

        for (TreeDefinition tree : TREES) {
            tree.generate(random, blockX, blockZ, world, dimensionId);
        }
    }

    private static TreeDefinition tree(int dimensionId, String biomeName, String logName, String leavesName,
            int minHeight, int extraHeight, int frequency, int attempts, String baseBlockName) {
        return tree(dimensionId, biomeName, local(logName), local(leavesName), minHeight, extraHeight, frequency,
                attempts, baseBlockName);
    }

    private static TreeDefinition tree(int dimensionId, String biomeName, ResourceLocation logId,
            ResourceLocation leavesId, int minHeight, int extraHeight, int frequency, int attempts,
            String baseBlockName) {
        return new TreeDefinition(dimensionId, local(biomeName), logId, leavesId, minHeight, extraHeight, frequency,
                attempts, local(baseBlockName));
    }

    private static ResourceLocation local(String name) {
        return new ResourceLocation(MOD_ID, name);
    }

    private static ResourceLocation external(String modId, String name) {
        return new ResourceLocation(modId, name);
    }

    private static final class TreeDefinition {
        private final int dimensionId;
        private final ResourceLocation biomeId;
        private final ResourceLocation logId;
        private final ResourceLocation leavesId;
        private final int minHeight;
        private final int extraHeight;
        private final int frequency;
        private final int attempts;
        private final ResourceLocation baseBlockId;

        private TreeDefinition(int dimensionId, ResourceLocation biomeId, ResourceLocation logId,
                ResourceLocation leavesId, int minHeight, int extraHeight, int frequency, int attempts,
                ResourceLocation baseBlockId) {
            this.dimensionId = dimensionId;
            this.biomeId = biomeId;
            this.logId = logId;
            this.leavesId = leavesId;
            this.minHeight = minHeight;
            this.extraHeight = extraHeight;
            this.frequency = Math.max(1, frequency);
            this.attempts = Math.max(0, attempts);
            this.baseBlockId = baseBlockId;
        }

        private void generate(Random random, int blockX, int blockZ, World world, int currentDimensionId) {
            if (currentDimensionId != dimensionId || random.nextInt(frequency) != 0) {
                return;
            }

            Block log = block(logId);
            Block leaves = block(leavesId);
            Block baseBlock = block(baseBlockId);
            Biome biome = biome(biomeId);
            if (log == null || leaves == null || baseBlock == null || biome == null) {
                return;
            }

            WorldGenAlfheimTree generator = new WorldGenAlfheimTree(minHeight, extraHeight, log.getDefaultState(),
                    leaves.getDefaultState(), baseBlock);
            for (int i = 0; i < attempts; i++) {
                BlockPos pos = findSurfacePosition(random, blockX, blockZ, world, biome, baseBlock);
                if (pos != null) {
                    generator.generate(world, random, pos);
                }
            }
        }

        @Nullable
        private BlockPos findSurfacePosition(Random random, int blockX, int blockZ, World world, Biome biome,
                Block baseBlock) {
            int x = blockX + random.nextInt(16) + 8;
            int z = blockZ + random.nextInt(16) + 8;
            BlockPos surface = world.getHeight(new BlockPos(x, 0, z));
            BlockPos ground = surface.down();
            if (world.getBiome(surface) != biome || world.getBlockState(ground).getBlock() != baseBlock) {
                return null;
            }
            return surface;
        }

        @Nullable
        private static Block block(ResourceLocation id) {
            Block block = ForgeRegistries.BLOCKS.getValue(id);
            return block == null || block == Blocks.AIR ? null : block;
        }

        @Nullable
        private static Biome biome(ResourceLocation id) {
            return ForgeRegistries.BIOMES.getValue(id);
        }
    }
}
