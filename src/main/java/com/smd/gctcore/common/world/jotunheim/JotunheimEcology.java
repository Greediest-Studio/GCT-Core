package com.smd.gctcore.common.world.jotunheim;

import com.smd.gctcore.common.world.jotunheim.JotunheimTerrainKernel.HeightLayer;

/**
 * Stable 8x4 ecological addressing without changing vanilla's two-dimensional biome array.
 * Stage three defines substrate only; decorations, spawns and local weather are deliberately absent.
 */
public final class JotunheimEcology {
    public static final class Profile {
        public final int biomeIndex;
        public final HeightLayer layer;
        public final String rock;
        public final String top;
        public final String filler;

        private Profile(int biomeIndex, HeightLayer layer, String rock, String top, String filler) {
            this.biomeIndex = biomeIndex;
            this.layer = layer;
            this.rock = rock;
            this.top = top;
            this.filler = filler;
        }

        public int index() { return biomeIndex * 4 + layer.ordinal(); }
    }

    private static final Profile[][] PROFILES = new Profile[8][4];

    static {
        // Columns in each row: lava, deep, surface, cloud.
        row(0, new String[][] {
                {"basalt", "volcanic_ash", "volcanic_rock"},
                {"giantstone", "jotunn_moss", "humus"},
                {"giantstone", "jotun_grass", "jotun_dirt"},
                {"cloudrock", "jotun_grass", "jotun_dirt"}});
        row(1, new String[][] {
                {"obsidian_rock", "windworn_stone", "volcanic_rock"},
                {"deep_ice_crystal", "ancient_ice", "jotun_permafrost"},
                {"giantstone", "jotun_permafrost", "jotun_permafrost"},
                {"cloudrock", "ancient_ice", "jotun_permafrost"}});
        row(2, new String[][] {
                {"volcanic_rock", "windworn_stone", "volcanic_rock"},
                {"giantstone", "windworn_stone", "giantstone"},
                {"giantstone", "windworn_stone", "jotun_dirt"},
                {"cloudrock", "windworn_stone", "cloudrock"}});
        row(3, new String[][] {
                {"volcanic_rock", "volcanic_ash", "obsidian_rock"},
                {"giantstone", "rotten_moss", "humus"},
                {"giantstone", "jotunn_moss", "humus"},
                {"cloudrock", "rotten_moss", "humus"}});
        row(4, new String[][] {
                {"crimson_crystal_stone", "crimson_crystal_stone", "obsidian_rock"},
                {"violet_crystal_stone", "azure_crystal_stone", "violet_crystal_stone"},
                {"giantstone", "crystal_sand", "azure_crystal_stone"},
                {"cloudrock", "crystal_sand", "azure_crystal_stone"}});
        row(5, new String[][] {
                {"thunderstone", "magma", "obsidian_rock"},
                {"thunderstone", "thunderstone", "giantstone"},
                {"giantstone", "thunderstone", "windworn_stone"},
                {"cloudrock", "thunderstone", "cloudrock"}});
        row(6, new String[][] {
                {"basalt", "magma", "volcanic_rock"},
                {"basalt", "volcanic_ash", "volcanic_rock"},
                {"volcanic_rock", "volcanic_ash", "volcanic_rock"},
                {"cloudrock", "volcanic_ash", "volcanic_rock"}});
        row(7, new String[][] {
                {"obsidian_rock", "weathered_giantstone", "giantstone"},
                {"weathered_giantstone", "weathered_giantstone", "giantstone"},
                {"giantstone", "weathered_giantstone", "jotun_dirt"},
                {"weathered_giantstone", "weathered_giantstone", "cloudrock"}});
    }

    private JotunheimEcology() {}

    private static void row(int biome, String[][] materials) {
        for (HeightLayer layer : HeightLayer.values()) {
            String[] keys = materials[layer.ordinal()];
            PROFILES[biome][layer.ordinal()] = new Profile(biome, layer, keys[0], keys[1], keys[2]);
        }
    }

    public static Profile resolve(int biomeIndex, HeightLayer layer) {
        if (biomeIndex < 0 || biomeIndex >= 8 || layer == null) return null;
        return PROFILES[biomeIndex][layer.ordinal()];
    }

    /** Null in the empty gaps between generation bands. */
    public static Profile resolve(JotunheimTerrainKernel kernel, int x, int y, int z) {
        return resolve(kernel.biomeIndex(x, z), JotunheimTerrainKernel.layerAt(y));
    }
}
