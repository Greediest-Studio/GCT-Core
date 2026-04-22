package com.smd.gctcore.common.world;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fml.common.IWorldGenerator;

import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;

import java.util.Random;
import com.smd.gctcore.gctcore;

/**
 * Simple world generator that spawns a small "crimson_temple" structure on surface in the "beside_void" dimension.
 * This implementation is intentionally simple (block placements) to avoid external .nbt dependency.
 */
public class CrimsonTempleGenerator implements IWorldGenerator {

    // Generation probability: 1 in 2000 per chunk
    private static final int CHANCE = 2000; // 1 in CHANCE per chunk

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world == null) return;

        int dimId = world.provider.getDimension();

        

        // Only generate in dimension with ID 41
        if (dimId != 41) {
            return;
        }

        int roll = random.nextInt(CHANCE);
        if (roll != 0) return;

        int x = chunkX * 16 + random.nextInt(16);
        int z = chunkZ * 16 + random.nextInt(16);

        // Use top solid block (surface). Height offset = 0 (place starting at that Y)
        int y = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).getY();
        BlockPos pos = new BlockPos(x, y, z);

        // Don't generate on pure liquid surface (water or lava)
        if (world.getBlockState(pos).getMaterial().isLiquid()) {
            return;
        }

        // Load template from our own mod resources (gctcore) only
        if (!(world instanceof WorldServer)) return;
        WorldServer ws = (WorldServer) world;
        TemplateManager manager = ws.getStructureTemplateManager();
        Template template = null;
        try {
            template = manager.getTemplate(ws.getMinecraftServer(), new ResourceLocation("gctcore", "crimson_temple"));
        } catch (Exception ignored) {}

        if (template == null) {
            gctcore.LOGGER.warn("CrimsonTempleGenerator: no crimson_temple template found in gct_mobs or gctcore");
            // No template available; abort generation to avoid placing a fallback structure unexpectedly
            return;
        } else {
            gctcore.LOGGER.debug("CrimsonTempleGenerator: loaded crimson_temple template (from {})", template.getAuthor());
        }

        // Random rotation
        Rotation rotation = Rotation.values()[random.nextInt(Rotation.values().length)];
        PlacementSettings settings = new PlacementSettings();
        settings.setRotation(rotation);
        settings.setMirror(Mirror.NONE);

        // Place template at the top surface position without additional offset
        template.addBlocksToWorldChunk(world, pos, settings);

        // Persist the placed temple position into world saved data for dimension 41
        try {
            CrimsonTempleData data = CrimsonTempleData.get(world);
            if (data != null) {
                data.addTemple(pos);
            }
        } catch (Exception e) {
            gctcore.LOGGER.warn("CrimsonTempleGenerator: failed to record temple position: {}", e.getMessage());
        }
    }
}
