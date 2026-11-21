package com.smd.gctcore.common.world.biome.jotunheim;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.world.jotunheim.WorldProviderJotunheim;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Repairs only absent/invalid biome cells before a server chunk is sent to clients. */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class JotunheimBiomeRepair {
    private static final ResourceLocation REID_ERROR = new ResourceLocation("jeid", "error_biome");

    private JotunheimBiomeRepair() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onChunkLoad(ChunkEvent.Load event) {
        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();
        if (world.isRemote || !(world.provider instanceof WorldProviderJotunheim)) return;
        repair(chunk, world.getBiomeProvider());
    }

    static int repair(Chunk chunk, BiomeProvider provider) {
        int[] ids = JotunheimBiomeStorage.readIds(chunk);
        int repaired = 0;
        for (int i = 0; i < ids.length; i++) {
            Biome existing = ids[i] < 0 ? null : Biome.getBiome(ids[i]);
            // Preserve every other registered biome, including deliberate editor changes.
            if (existing != null && !REID_ERROR.equals(existing.getRegistryName())) continue;
            int x = chunk.x * 16 + (i & 15);
            int z = chunk.z * 16 + (i >> 4);
            Biome expected = provider.getBiome(new net.minecraft.util.math.BlockPos(x, 0, z));
            ids[i] = Biome.getIdForBiome(expected);
            repaired++;
        }
        if (repaired != 0) {
            JotunheimBiomeStorage.writeIds(chunk, ids);
            chunk.markDirty();
        }
        return repaired;
    }
}
