package com.smd.gctcore.common.world.biome.jotunheim;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.RegistryEvent;

public final class JotunheimBiomes {
    public static BiomeJotunheimBase JOTUNNWOOD;
    public static BiomeJotunheimBase FROSTWASTE;
    public static BiomeJotunheimBase GIANTS_HIGHLANDS;
    public static BiomeJotunheimBase MISTFEN;
    public static BiomeJotunheimBase CRYSTAL_BARRENS;
    public static BiomeJotunheimBase STORMREACH;
    public static BiomeJotunheimBase OBSIDIAN_CALDERA;
    public static BiomeJotunheimBase ELDER_WASTES;
    private static BiomeJotunheimBase[] all;

    private JotunheimBiomes() {}

    public static void init() {
        if (all != null) return;
        JOTUNNWOOD = new BiomeJotunheimBase(0, "jotunheim_jotunnwood", "Jotunnwood", "jotun_grass", "jotun_dirt",
                0.45F, 0.8F, 0x849EA8, 0x647C64, 0x546F60);
        FROSTWASTE = new BiomeJotunheimBase(1, "jotunheim_frostwaste", "Frostwaste", "jotun_permafrost", "jotun_permafrost",
                0.0F, 0.15F, 0xBCCED8, 0x99A7A6, 0x9AACAE);
        GIANTS_HIGHLANDS = new BiomeJotunheimBase(2, "jotunheim_giants_highlands", "Giant Highlands", "windworn_stone", "jotun_dirt",
                0.3F, 0.3F, 0xA1B1B7, 0x879080, 0x798975);
        MISTFEN = new BiomeJotunheimBase(3, "jotunheim_mistfen", "Mistfen", "jotunn_moss", "humus",
                0.55F, 0.95F, 0x7F9692, 0x5A7264, 0x536E61);
        CRYSTAL_BARRENS = new BiomeJotunheimBase(4, "jotunheim_crystal_barrens", "Crystal Barrens", "crystal_sand", "azure_crystal_stone",
                0.4F, 0.1F, 0x9EAFBF, 0x8898A0, 0x7E96A0);
        STORMREACH = new BiomeJotunheimBase(5, "jotunheim_stormreach", "Stormreach", "thunderstone", "windworn_stone",
                0.35F, 0.7F, 0x778794, 0x6C7C74, 0x60766B);
        OBSIDIAN_CALDERA = new BiomeJotunheimBase(6, "jotunheim_obsidian_caldera", "Obsidian Caldera", "volcanic_ash", "volcanic_rock",
                1.2F, 0.0F, 0x8B8389, 0x7D746D, 0x726E65);
        ELDER_WASTES = new BiomeJotunheimBase(7, "jotunheim_elder_wastes", "Elder Wastes", "weathered_giantstone", "jotun_dirt",
                0.4F, 0.25F, 0xA5A69C, 0x848976, 0x7B8470);
        all = new BiomeJotunheimBase[] {JOTUNNWOOD, FROSTWASTE, GIANTS_HIGHLANDS, MISTFEN,
                CRYSTAL_BARRENS, STORMREACH, OBSIDIAN_CALDERA, ELDER_WASTES};
    }

    public static BiomeJotunheimBase byIndex(int index) {
        if (all == null) throw new IllegalStateException("JotunheimBiomes.init must run after Jotunheim block initialization");
        if (index < 0 || index >= all.length) throw new IllegalArgumentException("Invalid Jotunheim biome index " + index);
        return all[index];
    }

    public static BiomeJotunheimBase[] all() { return all == null ? new BiomeJotunheimBase[0] : all.clone(); }

    public static void register(RegistryEvent.Register<Biome> event) {
        if (all == null) throw new IllegalStateException("Jotunheim biomes were not initialized before biome registration");
        event.getRegistry().registerAll(all);
        BiomeDictionary.addTypes(JOTUNNWOOD, BiomeDictionary.Type.FOREST, BiomeDictionary.Type.DENSE, BiomeDictionary.Type.MOUNTAIN);
        BiomeDictionary.addTypes(FROSTWASTE, BiomeDictionary.Type.COLD, BiomeDictionary.Type.SNOWY, BiomeDictionary.Type.SPARSE);
        BiomeDictionary.addTypes(GIANTS_HIGHLANDS, BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.DRY);
        BiomeDictionary.addTypes(MISTFEN, BiomeDictionary.Type.SWAMP, BiomeDictionary.Type.WET);
        BiomeDictionary.addTypes(CRYSTAL_BARRENS, BiomeDictionary.Type.DRY, BiomeDictionary.Type.MAGICAL, BiomeDictionary.Type.SPARSE);
        BiomeDictionary.addTypes(STORMREACH, BiomeDictionary.Type.MOUNTAIN, BiomeDictionary.Type.WET);
        BiomeDictionary.addTypes(OBSIDIAN_CALDERA, BiomeDictionary.Type.HOT, BiomeDictionary.Type.DRY, BiomeDictionary.Type.WASTELAND);
        BiomeDictionary.addTypes(ELDER_WASTES, BiomeDictionary.Type.WASTELAND, BiomeDictionary.Type.SPARSE);
    }
}
