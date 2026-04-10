package com.smd.gctcore.common.world;

import com.smd.gctcore.gctcore;
import net.minecraft.block.material.Material;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;

public class ShadowberryCaveGenerator implements IWorldGenerator {

    private static final int TARGET_DIMENSION_ID = 42;
    private static final int CHANCE = 200;
    private static final int MAX_GENERATION_Y = 39;
    private static final int MIN_GENERATION_Y = 8;
    private static final int PLACEMENT_ATTEMPTS = 6;
    private static final String MINOR_TEMPLATE_NAME = "shadowberry_cave_minor";
    private static final String BIGGER_TEMPLATE_NAME = "shadowberry_cave_bigger";
    private static final int MINOR_WEIGHT = 1;
    private static final int BIGGER_WEIGHT = 3;

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world == null || world.provider.getDimension() != TARGET_DIMENSION_ID) {
            return;
        }

        if (random.nextInt(CHANCE) != 0) {
            return;
        }

        if (!(world instanceof WorldServer)) {
            return;
        }

        BlockPos pos = findPlacementPos(random, chunkX, chunkZ, world);
        if (pos == null) {
            return;
        }

        WorldServer worldServer = (WorldServer) world;
        String templateName = selectTemplateName(random);
        Template template = loadTemplate(worldServer, templateName);
        if (template == null) {
            gctcore.LOGGER.warn("ShadowberryCaveGenerator: missing {} template", templateName);
            return;
        }

        PlacementSettings settings = new PlacementSettings();
        settings.setRotation(Rotation.values()[random.nextInt(Rotation.values().length)]);
        settings.setMirror(Mirror.NONE);

        template.addBlocksToWorldChunk(world, pos, settings);

        try {
            ShadowberryCaveData data = ShadowberryCaveData.get(world);
            if (data != null) {
                data.addCave(pos);
            }
        } catch (Exception e) {
            gctcore.LOGGER.warn("ShadowberryCaveGenerator: failed to record cave position: {}", e.getMessage());
        }
    }

    private BlockPos findPlacementPos(Random random, int chunkX, int chunkZ, World world) {
        for (int attempt = 0; attempt < PLACEMENT_ATTEMPTS; attempt++) {
            int x = chunkX * 16 + random.nextInt(16);
            int y = MIN_GENERATION_Y + random.nextInt(MAX_GENERATION_Y - MIN_GENERATION_Y + 1);
            int z = chunkZ * 16 + random.nextInt(16);
            BlockPos pos = new BlockPos(x, y, z);
            Material material = world.getBlockState(pos).getMaterial();

            if (material.isLiquid() || material == Material.AIR) {
                continue;
            }

            return pos;
        }

        return null;
    }

    private String selectTemplateName(Random random) {
        int totalWeight = MINOR_WEIGHT + BIGGER_WEIGHT;
        int roll = random.nextInt(totalWeight);
        return roll < MINOR_WEIGHT ? MINOR_TEMPLATE_NAME : BIGGER_TEMPLATE_NAME;
    }

    private Template loadTemplate(WorldServer world, String templateName) {
        TemplateManager manager = world.getStructureTemplateManager();
        try {
            return manager.getTemplate(world.getMinecraftServer(), new ResourceLocation("gctcore", templateName));
        } catch (Exception ignored) {
            return null;
        }
    }
}