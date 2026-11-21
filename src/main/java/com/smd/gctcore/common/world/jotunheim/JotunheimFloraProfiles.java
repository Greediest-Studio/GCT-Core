package com.smd.gctcore.common.world.jotunheim;

import com.smd.gctcore.common.world.jotunheim.JotunheimTerrainKernel.HeightLayer;

import java.util.Random;

/** Natural decorations for each of the eight XZ biomes at each of the four height strata. */
public final class JotunheimFloraProfiles {
    public enum Tree {
        NONE, OAK, ELDER_PINE, SUNKENWOOD, WHITE_OAK, CHARRED, LIGHTNING, FUNGAL, EMBER_FUNGAL
    }

    public static final class Profile {
        public final int biomeIndex;
        public final HeightLayer layer;
        /** Weighted choices: repeated entries are deliberately more common. */
        public final Tree[] trees;
        public final String[] plants;
        /** Probability per large-feature candidate, with the count controlled by treeCandidates. */
        public final double treeChance;
        /** Seeded large-feature candidates per 32 x 32 cell at this height. */
        public final int treeCandidates;
        /** Remaining chances are per eligible ground column, before placement checks. */
        public final double plantChance;
        public final String groundCover;
        public final double groundCoverChance;
        public final String vineKey;
        public final double vineChance;
        public final double reedChance;
        public final String crystalKey;
        public final double crystalChance;
        public final double iceSpikeChance;
        public final double groundFungusChance;
        public final String cloudKey;
        public final double cloudChance;
        public final double rootChance;

        private Profile(int biomeIndex, HeightLayer layer, Builder b) {
            this.biomeIndex = biomeIndex;
            this.layer = layer;
            trees = b.trees.clone();
            plants = b.plants.clone();
            treeChance = b.treeChance;
            treeCandidates = b.treeCandidates;
            plantChance = b.plantChance;
            groundCover = b.groundCover;
            groundCoverChance = b.groundCoverChance;
            vineKey = b.vineKey;
            vineChance = b.vineChance;
            reedChance = b.reedChance;
            crystalKey = b.crystalKey;
            crystalChance = b.crystalChance;
            iceSpikeChance = b.iceSpikeChance;
            groundFungusChance = b.groundFungusChance;
            cloudKey = b.cloudKey;
            cloudChance = b.cloudChance;
            rootChance = b.rootChance;
        }

        public Tree tree(Random random) {
            return trees.length == 0 ? Tree.NONE : trees[random.nextInt(trees.length)];
        }

        public String plant(Random random) {
            return plants.length == 0 ? null : plants[random.nextInt(plants.length)];
        }

        public int index() { return biomeIndex * 4 + layer.ordinal(); }
    }

    private static final class Builder {
        final Tree[] trees;
        final String[] plants;
        final double treeChance, plantChance;
        String groundCover, vineKey, crystalKey, cloudKey;
        double groundCoverChance, vineChance, reedChance, crystalChance, iceSpikeChance;
        double groundFungusChance, cloudChance, rootChance;
        int treeCandidates = 2;

        Builder(double treeChance, Tree[] trees, double plantChance, String... plants) {
            this.treeChance = treeChance;
            this.trees = trees;
            this.plantChance = plantChance;
            this.plants = plants;
        }
        Builder cover(String key, double chance) { groundCover = key; groundCoverChance = chance; return this; }
        Builder vines(String key, double chance) { vineKey = key; vineChance = chance; return this; }
        Builder reeds(double chance) { reedChance = chance; return this; }
        Builder crystals(String key, double chance) { crystalKey = key; crystalChance = chance; return this; }
        Builder ice(double chance) { iceSpikeChance = chance; return this; }
        Builder fungi(double chance) { groundFungusChance = chance; return this; }
        Builder clouds(String key, double chance) { cloudKey = key; cloudChance = chance; return this; }
        Builder roots(double chance) { rootChance = chance; return this; }
        Builder density(int candidates) { treeCandidates = candidates; return this; }
    }

    private static final Profile[][] PROFILES = new Profile[8][4];

    static {
        // Giant forest: burnt roots below, a fungal understorey, broad oak woodland and white cloud crowns.
        put(0, HeightLayer.LAVA, plants(0.22, trees(Tree.CHARRED, Tree.CHARRED, Tree.EMBER_FUNGAL), 0.045,
                "ash_fern", "ember_mushroom").fungi(0.018).roots(0.012));
        put(0, HeightLayer.DEEP, plants(0.88, trees(Tree.FUNGAL), 0.13,
                "rootshade_fern", "rootshade_fern", "mistcup_fungus", "pale_droopflower")
                .density(6).cover("luminous_moss", 0.14).fungi(0.025).vines("mistvine", 0.009).roots(0.016));
        put(0, HeightLayer.SURFACE, plants(0.98, trees(Tree.OAK, Tree.OAK, Tree.OAK, Tree.ELDER_PINE), 0.16,
                "giant_seedgrass", "giant_seedgrass", "rootshade_fern", "pale_droopflower")
                .density(8).cover("jotunn_moss", 0.10).vines("cloudvine", 0.010).roots(0.006));
        put(0, HeightLayer.CLOUD, plants(0.82, trees(Tree.WHITE_OAK), 0.11,
                "cloud_bellflower", "cloud_bellflower", "windwool_flower", "pale_droopflower")
                .density(6).cover("rotten_moss", 0.08).vines("cloudvine", 0.016).roots(0.004));

        // Frostwaste: sparse pine taiga, glacier flowers, deep ice needles and frozen cloud shelves.
        put(1, HeightLayer.LAVA, plants(0.06, trees(Tree.CHARRED), 0.007,
                "ember_mushroom", "ash_fern").ice(0.011).fungi(0.002));
        put(1, HeightLayer.DEEP, plants(0.0, trees(), 0.022,
                "frost_cup_flower", "silverneedle_grass")
                .ice(0.030).crystals("deep_ice_crystal", 0.006));
        put(1, HeightLayer.SURFACE, plants(0.46, trees(Tree.ELDER_PINE), 0.058,
                "silverneedle_grass", "silverneedle_grass", "frost_cup_flower", "windwool_flower")
                .ice(0.010).crystals("deep_ice_crystal", 0.004));
        put(1, HeightLayer.CLOUD, plants(0.12, trees(Tree.ELDER_PINE), 0.042,
                "frost_cup_flower", "windwool_flower")
                .ice(0.035).crystals("deep_ice_crystal", 0.002).clouds("frostbound_aercloud", 0.035));

        // Highlands retain open meadows and rock gardens rather than becoming another dense forest.
        put(2, HeightLayer.LAVA, plants(0.08, trees(Tree.CHARRED), 0.020,
                "ash_fern", "ember_mushroom").crystals("volcanic_rock", 0.004));
        put(2, HeightLayer.DEEP, plants(0.0, trees(), 0.030,
                "crag_blossom", "silverneedle_grass", "rune_lichen")
                .crystals("natural_crystal", 0.010).roots(0.004));
        put(2, HeightLayer.SURFACE, plants(0.24, trees(Tree.ELDER_PINE), 0.10,
                "giant_seedgrass", "silverneedle_grass", "crag_blossom")
                .cover("jotun_grass", 0.45).roots(0.001));
        put(2, HeightLayer.CLOUD, plants(0.08, trees(Tree.ELDER_PINE), 0.052,
                "windwool_flower", "crag_blossom", "silverneedle_grass"));

        // Mistfen: tall reeds, wet ground cover, sunkenwood roots and hanging vegetation in every cool layer.
        put(3, HeightLayer.LAVA, plants(0.12, trees(Tree.CHARRED, Tree.EMBER_FUNGAL), 0.035,
                "ash_fern", "ember_mushroom").fungi(0.025).roots(0.005));
        put(3, HeightLayer.DEEP, plants(0.65, trees(Tree.FUNGAL, Tree.FUNGAL, Tree.SUNKENWOOD), 0.19,
                "mistcup_fungus", "mistcup_fungus", "fen_lantern_sedge", "rootshade_fern")
                .cover("rotten_moss", 0.14).reeds(0.007).vines("mistvine", 0.020).roots(0.012).fungi(0.030));
        put(3, HeightLayer.SURFACE, plants(0.82, trees(Tree.SUNKENWOOD), 0.22,
                "fen_lantern_sedge", "fen_lantern_sedge", "giant_seedgrass", "mistcup_fungus")
                .cover("humus", 0.12).reeds(0.020).vines("mistvine", 0.022).roots(0.014).fungi(0.015));
        put(3, HeightLayer.CLOUD, plants(0.46, trees(Tree.SUNKENWOOD, Tree.WHITE_OAK), 0.17,
                "cloud_bellflower", "fen_lantern_sedge", "pale_droopflower")
                .cover("rotten_moss", 0.11).reeds(0.008).vines("cloudvine", 0.025).roots(0.005));

        // Crystal barrens: the dominant tall features are crystal outcrops, not ordinary trees.
        put(4, HeightLayer.LAVA, plants(0.0, trees(), 0.012,
                "ember_mushroom", "rune_lichen").crystals("crimson_crystal_stone", 0.034));
        put(4, HeightLayer.DEEP, plants(0.05, trees(Tree.FUNGAL), 0.055,
                "crystal_fern", "violet_starflower")
                .crystals("azure_crystal_stone", 0.055).fungi(0.004));
        put(4, HeightLayer.SURFACE, plants(0.0, trees(), 0.050,
                "crystal_fern", "violet_starflower", "crag_blossom")
                .crystals("natural_crystal", 0.048));
        put(4, HeightLayer.CLOUD, plants(0.0, trees(), 0.035,
                "windwool_flower", "crystal_fern", "violet_starflower")
                .crystals("clear_crystal", 0.044));

        // Stormreach: open wind grass, split lightning-struck trees and conductive crystalline rocks.
        put(5, HeightLayer.LAVA, plants(0.0, trees(), 0.018,
                "ash_fern", "thunderwhisker_grass", "ember_mushroom")
                .crystals("thunderstone", 0.020));
        put(5, HeightLayer.DEEP, plants(0.0, trees(), 0.038,
                "crystal_fern", "thunderwhisker_grass")
                .crystals("azure_crystal_stone", 0.030));
        put(5, HeightLayer.SURFACE, plants(0.26, trees(Tree.LIGHTNING), 0.075,
                "thunderwhisker_grass", "thunderwhisker_grass", "windwool_flower", "crag_blossom")
                .cover("jotun_grass", 0.15).roots(0.003));
        put(5, HeightLayer.CLOUD, plants(0.18, trees(Tree.LIGHTNING), 0.065,
                "thunderwhisker_grass", "windwool_flower")
                .clouds("thundering_aercloud", 0.055).crystals("thunderstone", 0.015).roots(0.001));

        // Volcano: charcoal trunks and ember fungi survive among sparse ash ferns.
        put(6, HeightLayer.LAVA, plants(0.08, trees(Tree.CHARRED), 0.008,
                "ember_mushroom").fungi(0.012).crystals("crimson_crystal_stone", 0.018));
        put(6, HeightLayer.DEEP, plants(0.14, trees(Tree.CHARRED, Tree.EMBER_FUNGAL), 0.035,
                "ash_fern", "ember_mushroom")
                .fungi(0.020).roots(0.004).crystals("crimson_crystal_stone", 0.007));
        put(6, HeightLayer.SURFACE, plants(0.20, trees(Tree.CHARRED), 0.045,
                "ash_fern", "ash_fern", "ember_mushroom")
                .roots(0.005).fungi(0.010));
        put(6, HeightLayer.CLOUD, plants(0.10, trees(Tree.CHARRED), 0.025,
                "ash_fern", "windwool_flower")
                .clouds("ashen_aercloud", 0.045).fungi(0.002).crystals("volcanic_rock", 0.008));

        // Elder wastes: lichen and sparse reclaiming woodland; no artificial ruins are generated here.
        put(7, HeightLayer.LAVA, plants(0.04, trees(Tree.CHARRED), 0.015,
                "rune_lichen", "ember_mushroom")
                .crystals("crimson_crystal_stone", 0.006).roots(0.002).fungi(0.004));
        put(7, HeightLayer.DEEP, plants(0.0, trees(), 0.040,
                "rune_lichen", "rune_lichen", "crystal_fern")
                .crystals("natural_crystal", 0.010).roots(0.004).fungi(0.008));
        put(7, HeightLayer.SURFACE, plants(0.14, trees(Tree.OAK, Tree.ELDER_PINE), 0.075,
                "rune_lichen", "rune_lichen", "crag_blossom", "giant_seedgrass")
                .cover("humus", 0.025).roots(0.007));
        put(7, HeightLayer.CLOUD, plants(0.04, trees(Tree.WHITE_OAK), 0.035,
                "rune_lichen", "windwool_flower")
                .crystals("natural_crystal", 0.010).roots(0.002));
    }

    private JotunheimFloraProfiles() {}

    private static Tree[] trees(Tree... trees) { return trees; }

    private static Builder plants(double treeChance, Tree[] trees, double plantChance, String... plants) {
        return new Builder(treeChance, trees, plantChance, plants);
    }

    private static void put(int biome, HeightLayer layer, Builder builder) {
        PROFILES[biome][layer.ordinal()] = new Profile(biome, layer, builder);
    }

    public static Profile resolve(int biomeIndex, HeightLayer layer) {
        if (biomeIndex < 0 || biomeIndex >= PROFILES.length || layer == null) return null;
        return PROFILES[biomeIndex][layer.ordinal()];
    }
}
