package com.smd.gctcore.misc.moretcon;

import com.smd.gctcore.common.config.GctCoreConfig;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class to check if a block should be treated as bedrock-like
 * based on the configuration whitelist.
 */
public class BedrockBlockChecker {

    private static final Map<String, BlockEntry> cachedEntries = new HashMap<>();
    private static boolean needsRefresh = true;

    /**
     * Checks if the given block state is in the bedrock-like blocks list.
     */
    public static boolean isBedrockLike(@Nonnull IBlockState state) {
        Block block = state.getBlock();
        
        // Check if block implements IGctBedrockMineable interface
        if (block instanceof IGctBedrockMineable) {
            return ((IGctBedrockMineable) block).isGctBedrockLike(state, null, null);
        }
        
        // Check configuration whitelist
        if (needsRefresh) {
            refreshCache();
        }

        ResourceLocation registryName = block.getRegistryName();
        if (registryName == null) {
            return false;
        }

        String blockId = registryName.toString();
        int meta = block.getMetaFromState(state);

        // Check exact match with metadata
        String exactKey = blockId + "@" + meta;
        BlockEntry e = cachedEntries.get(exactKey);
        if (e != null) {
            return true;
        }

        // Check wildcard metadata
        String wildcardKey = blockId + "@*";
        e = cachedEntries.get(wildcardKey);
        if (e != null) {
            return true;
        }

        // Check no-metadata match (matches all metadata)
        e = cachedEntries.get(blockId);
        if (e != null) {
            return true;
        }

        return false;
    }

    /**
     * Returns whether bedrock-like blocks should be treated as soft bedrock.
     */
    public static boolean isSoftBedrock(@Nonnull IBlockState state) {
        Block block = state.getBlock();
        
        // Check if block implements IGctBedrockMineable interface
        if (block instanceof IGctBedrockMineable) {
            return ((IGctBedrockMineable) block).isGctSoftBedrock(state, null, null);
        }
        
        // Otherwise consult per-entry flag if present
        if (needsRefresh) {
            refreshCache();
        }

        ResourceLocation registryName = block.getRegistryName();
        if (registryName == null) {
            return GctCoreConfig.moreTconIntegration.treatAsSoftBedrock;
        }

        String blockId = registryName.toString();
        int meta = block.getMetaFromState(state);

        // exact
        BlockEntry e = cachedEntries.get(blockId + "@" + meta);
        if (e != null && e.forcedSoft != null) {
            return e.forcedSoft;
        }

        // wildcard
        e = cachedEntries.get(blockId + "@*");
        if (e != null && e.forcedSoft != null) {
            return e.forcedSoft;
        }

        // no-meta
        e = cachedEntries.get(blockId);
        if (e != null && e.forcedSoft != null) {
            return e.forcedSoft;
        }

        // fallback to global config
        return GctCoreConfig.moreTconIntegration.treatAsSoftBedrock;
    }

    /**
     * Marks the cache as needing refresh (called when config changes).
     */
    public static void markDirty() {
        needsRefresh = true;
    }

    /**
     * Refreshes the cached entries from the config.
     */
    private static void refreshCache() {
        cachedEntries.clear();
        
        String[] blocks = GctCoreConfig.moreTconIntegration.bedrockLikeBlocks;
        if (blocks == null || blocks.length == 0) {
            needsRefresh = false;
            return;
        }

        for (String entry : blocks) {
            if (entry == null || entry.trim().isEmpty()) {
                continue;
            }

            entry = entry.trim();
            BlockEntry blockEntry = parseEntry(entry);
            if (blockEntry != null) {
                cachedEntries.put(blockEntry.key, blockEntry);
            }
        }

        needsRefresh = false;
    }

    /**
     * Parses a block entry string into a BlockEntry object.
     * Format: modid:blockid@metadata
     * Examples:
     *   minecraft:stone@0
     *   minecraft:wool@*
     *   minecraft:obsidian (no metadata = all metadata)
     */
    private static BlockEntry parseEntry(String entry) {
        try {
            String blockId;
            String metadata = null;

            if (entry.contains("@")) {
                String[] parts = entry.split("@", 2);
                blockId = parts[0].trim();
                metadata = parts[1].trim();
            } else {
                blockId = entry;
            }

            // Validate format (must contain colon for modid:blockid)
            if (!blockId.contains(":")) {
                System.err.println("[Gct-Core] Invalid block entry format (missing modid): " + entry);
                return null;
            }

            String key;
            // support optional per-entry suffix :soft or :hard
            Boolean forcedSoft = null;
            if (metadata != null && !metadata.isEmpty()) {
                // metadata may contain ":soft" or ":hard" suffix
                String metaPart = metadata;
                String flag = null;
                if (metadata.contains(":")) {
                    String[] mp = metadata.split(":", 2);
                    metaPart = mp[0].trim();
                    flag = mp[1].trim().toLowerCase();
                }

                if (metaPart.isEmpty()) {
                    key = blockId;
                } else {
                    key = blockId + "@" + metaPart;
                }

                if (flag != null) {
                    if (flag.equals("soft")) forcedSoft = true;
                    else if (flag.equals("hard")) forcedSoft = false;
                }
                return new BlockEntry(key, blockId, metaPart, forcedSoft);
            } else {
                key = blockId;
                return new BlockEntry(key, blockId, null, null);
            }

        } catch (Exception e) {
            System.err.println("[Gct-Core] Failed to parse block entry: " + entry);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Internal class representing a parsed block entry.
     */
    private static class BlockEntry {
        final String key;
        final String blockId;
        final String metadata;
        final Boolean forcedSoft; // null = not specified, true = soft, false = hard

        BlockEntry(String key, String blockId, String metadata, Boolean forcedSoft) {
            this.key = key;
            this.blockId = blockId;
            this.metadata = metadata;
            this.forcedSoft = forcedSoft;
        }
    }
}
