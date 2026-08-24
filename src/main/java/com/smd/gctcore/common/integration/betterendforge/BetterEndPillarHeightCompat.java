package com.smd.gctcore.common.integration.betterendforge;

import com.smd.gctcore.common.mixin.betterendforge.AccessorEndSpike;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeEndDecorator;
import net.minecraft.world.gen.feature.WorldGenSpikes;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * Bridges BetterEndForge's volatile WorldDataAPI pillar cache to world data.
 *
 * <p>The affected BetterEndForge version stores the base Y of each spike only
 * in a static NBT compound. After a restart that compound is empty, so the mod
 * mistakes the existing pillar top for a new base and stacks another pillar on
 * it. This class makes the saved value authoritative and can recover old saves
 * from the decorative base structure that BetterEndForge places around every
 * custom spike.</p>
 */
public final class BetterEndPillarHeightCompat {

    private static final String BETTER_END_DOMAIN = "betterendforge";

    /* Base template widths for pillar_base_1 through pillar_base_4. */
    private static final int[] BASE_TEMPLATE_WIDTHS = {7, 11, 9, 9};

    /*
     * Three immutable flavolite blocks on the lowest decorative layer of each
     * base template. Coordinates are template-local X/Z values.
     */
    private static final int[][][] BASE_MARKERS = {
            {{0, 1}, {4, 5}, {6, 2}},
            {{1, 3}, {3, 1}, {9, 9}},
            {{0, 2}, {3, 1}, {8, 6}},
            {{0, 1}, {4, 0}, {8, 7}}
    };

    /* Local Y of the marker layer in each base template. */
    private static final int[] BASE_MARKER_Y = {2, 2, 2, 3};

    /* Center bedrock Y in the matching BetterEndForge top templates. */
    private static final int[] TOP_BEDROCK_Y = {11, 14, 12, 21};
    private static final int[] GUARDED_TOP_BEDROCK_Y = {8, 16, 12, 21};

    private BetterEndPillarHeightCompat() {}

    /**
     * Synchronizes BetterEndForge's returned "pillars" compound with the End
     * world's persistent data. Must only be invoked for that exact compound.
     */
    public static void synchronize(NBTTagCompound runtimeHeights) {
        if (runtimeHeights == null) {
            return;
        }

        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null || !server.isCallingFromMinecraftThread()) {
            return;
        }

        WorldServer endWorld = server.getWorld(1);
        if (endWorld == null) {
            return;
        }

        BetterEndPillarHeightData savedHeights = BetterEndPillarHeightData.get(endWorld);
        WorldGenSpikes.EndSpike[] spikes = BiomeEndDecorator.getSpikesForWorld(endWorld);

        for (WorldGenSpikes.EndSpike spike : spikes) {
            String key = pillarKey(spike);

            if (savedHeights.hasHeight(key)) {
                runtimeHeights.setInteger(key, savedHeights.getHeight(key));
                continue;
            }

            int recoveredHeight = recoverFromBaseTemplate(endWorld, spike);
            if (isValidHeight(recoveredHeight)) {
                saveAndPublish(savedHeights, runtimeHeights, key, recoveredHeight);
                continue;
            }

            if (runtimeHeights.hasKey(key, 3)) {
                int runtimeHeight = runtimeHeights.getInteger(key);
                if (isValidHeight(runtimeHeight)) {
                    savedHeights.setHeight(key, runtimeHeight);
                    continue;
                }
            }

            recoveredHeight = recoverFromTopBedrock(endWorld, spike);
            if (isValidHeight(recoveredHeight)) {
                saveAndPublish(savedHeights, runtimeHeights, key, recoveredHeight);
            }
        }
    }

    private static int recoverFromBaseTemplate(WorldServer world, WorldGenSpikes.EndSpike spike) {
        int templateIndex = getTemplateIndex(spike);
        if (templateIndex < 0) {
            return -1;
        }

        int originX = spike.getCenterX() - (BASE_TEMPLATE_WIDTHS[templateIndex] >> 1);
        int originZ = spike.getCenterZ() - (BASE_TEMPLATE_WIDTHS[templateIndex] >> 1);
        int[][] markers = BASE_MARKERS[templateIndex];

        for (int[] marker : markers) {
            BlockPos markerColumn = new BlockPos(originX + marker[0], 64, originZ + marker[1]);
            if (!world.isBlockLoaded(markerColumn, false)) {
                return -1;
            }
        }

        int markerY = BASE_MARKER_Y[templateIndex];
        int worldHeight = Math.min(world.getActualHeight(), 256);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int y = 0; y < worldHeight; y++) {
            boolean matches = true;
            for (int[] marker : markers) {
                mutable.setPos(originX + marker[0], y, originZ + marker[1]);
                if (!isFlavolite(world.getBlockState(mutable).getBlock())) {
                    matches = false;
                    break;
                }
            }

            if (matches) {
                // BetterEndForge places the template at baseY - 3.
                return y + 3 - markerY;
            }
        }
        return -1;
    }

    private static int recoverFromTopBedrock(WorldServer world, WorldGenSpikes.EndSpike spike) {
        int templateIndex = getTemplateIndex(spike);
        if (templateIndex < 0) {
            return -1;
        }

        BlockPos centerColumn = new BlockPos(spike.getCenterX(), 64, spike.getCenterZ());
        if (!world.isBlockLoaded(centerColumn, false)) {
            return -1;
        }

        int bedrockOffset = spike.isGuarded()
                ? GUARDED_TOP_BEDROCK_Y[templateIndex]
                : TOP_BEDROCK_Y[templateIndex];
        int rawSpikeHeight = ((AccessorEndSpike) spike).gctcore$getRawHeight();
        int worldHeight = Math.min(world.getActualHeight(), 256);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(spike.getCenterX(), 0, spike.getCenterZ());

        // The lowest surviving center bedrock belongs to the earliest pillar
        // top. Later broken respawns can leave additional tops above it.
        for (int y = 0; y < worldHeight; y++) {
            mutable.setY(y);
            if (world.getBlockState(mutable).getBlock() == Blocks.BEDROCK) {
                return y - bedrockOffset - rawSpikeHeight + 64;
            }
        }
        return -1;
    }

    private static boolean isFlavolite(Block block) {
        ResourceLocation registryName = block.getRegistryName();
        return registryName != null
                && BETTER_END_DOMAIN.equals(registryName.getNamespace())
                && "flavolite".equals(registryName.getPath());
    }

    private static int getTemplateIndex(WorldGenSpikes.EndSpike spike) {
        int index = spike.getRadius() - 2;
        return index >= 0 && index < BASE_TEMPLATE_WIDTHS.length ? index : -1;
    }

    private static String pillarKey(WorldGenSpikes.EndSpike spike) {
        return spike.getCenterX() + "_" + spike.getCenterZ();
    }

    private static void saveAndPublish(BetterEndPillarHeightData savedHeights,
                                       NBTTagCompound runtimeHeights,
                                       String key,
                                       int height) {
        savedHeights.setHeight(key, height);
        runtimeHeights.setInteger(key, height);
    }

    private static boolean isValidHeight(int height) {
        return height >= 0 && height < 256;
    }
}
