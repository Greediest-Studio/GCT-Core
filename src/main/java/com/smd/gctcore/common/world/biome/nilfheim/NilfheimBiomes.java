package com.smd.gctcore.common.world.biome.nilfheim;

import com.smd.gctcore.misc.BlockRegistry;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.RegistryEvent;

public class NilfheimBiomes {
    public static final int DIMENSION_ID = 43;

    public static BiomeNilfheimBase MISTVEIL_DEPTHS;
    public static BiomeNilfheimBase HOWLING_ICEFIELD;
    public static BiomeNilfheimBase SUNKEN_TEAR;
    public static BiomeNilfheimBase RIMEFANG_MOUNTAINS;
    public static BiomeNilfheimBase SOUL_EMBER_PLAINS;
    public static BiomeNilfheimBase GLIMMERING_HOLLOW;
    public static BiomeNilfheimBase HVERGELMIR_THROAT;

    public static void init() {
        MISTVEIL_DEPTHS = new BiomeNilfheimBase(
                "mistveil_depths", "Mistveil Depths",
                BlockRegistry.EROSION_MOSS.getDefaultState(),
                BlockRegistry.RIMESTEEL_STONE.getDefaultState(),
                0.35F, 0.85F, 0x586F83, 0x36475E, 0x7AA0A2, 0x5F8079);
        HOWLING_ICEFIELD = new BiomeNilfheimBase(
                "howling_icefield", "Howling Icefield",
                BlockRegistry.SOUL_ICE.getDefaultState(),
                BlockRegistry.PERMAFROST.getDefaultState(),
                0.08F, 0.05F, 0x83AFC5, 0xBCCAD8, 0xA6BBC2, 0x9AB1B8);
        SUNKEN_TEAR = new BiomeNilfheimBase(
                "sunken_tear", "Sunken Tear",
                BlockRegistry.EROSION_MOSS.getDefaultState(),
                BlockRegistry.RIMESTEEL_STONE.getDefaultState(),
                -0.75F, 0.18F, 0x506C83, 0x2C4259, 0x688C8D, 0x587773);
        RIMEFANG_MOUNTAINS = new BiomeNilfheimBase(
                "rimefang_mountains", "Rimefang Mountains",
                BlockRegistry.ANCIENT_SNOW.getDefaultState(),
                BlockRegistry.RIMESTEEL_STONE.getDefaultState(),
                1.85F, 1.25F, 0x5E7C91, 0x3D4658, 0xB5C3C2, 0xA9B8B9);
        SOUL_EMBER_PLAINS = new BiomeNilfheimBase(
                "soul_ember_plains", "Soul Ember Plains",
                BlockRegistry.ASHEN_SOIL.getDefaultState(),
                BlockRegistry.BLACK_RIME_STONE.getDefaultState(),
                0.18F, 0.22F, 0x4C6D84, 0x2A3848, 0x7B8889, 0x6F7977);
        GLIMMERING_HOLLOW = new BiomeNilfheimBase(
                "glimmering_hollow", "Glimmering Hollow",
                BlockRegistry.EROSION_MOSS.getDefaultState(),
                BlockRegistry.RIMESTEEL_STONE.getDefaultState(),
                0.2F, 0.55F, 0x4C7180, 0x1E3344, 0x7DB7AE, 0x669B91);
        HVERGELMIR_THROAT = new BiomeNilfheimBase(
                "hvergelmir_throat", "Hvergelmir's Throat",
                BlockRegistry.BOILING_MUD.getDefaultState(),
                BlockRegistry.BLACK_RIME_STONE.getDefaultState(),
                -0.15F, 0.8F, 0x5A6E72, 0x4F5160, 0x777A72, 0x6D6E64);
    }

    public static void register(RegistryEvent.Register<Biome> event) {
        event.getRegistry().registerAll(
                MISTVEIL_DEPTHS,
                HOWLING_ICEFIELD,
                SUNKEN_TEAR,
                RIMEFANG_MOUNTAINS,
                SOUL_EMBER_PLAINS,
                GLIMMERING_HOLLOW,
                HVERGELMIR_THROAT
        );
        BiomeDictionary.addTypes(MISTVEIL_DEPTHS, BiomeDictionary.Type.COLD, BiomeDictionary.Type.SNOWY, BiomeDictionary.Type.WET, BiomeDictionary.Type.MOUNTAIN);
        BiomeDictionary.addTypes(HOWLING_ICEFIELD, BiomeDictionary.Type.COLD, BiomeDictionary.Type.SNOWY, BiomeDictionary.Type.SPARSE);
        BiomeDictionary.addTypes(SUNKEN_TEAR, BiomeDictionary.Type.COLD, BiomeDictionary.Type.SNOWY, BiomeDictionary.Type.WATER, BiomeDictionary.Type.WET);
        BiomeDictionary.addTypes(RIMEFANG_MOUNTAINS, BiomeDictionary.Type.COLD, BiomeDictionary.Type.SNOWY, BiomeDictionary.Type.MOUNTAIN);
        BiomeDictionary.addTypes(SOUL_EMBER_PLAINS, BiomeDictionary.Type.COLD, BiomeDictionary.Type.SNOWY, BiomeDictionary.Type.DRY, BiomeDictionary.Type.PLAINS);
        BiomeDictionary.addTypes(GLIMMERING_HOLLOW, BiomeDictionary.Type.COLD, BiomeDictionary.Type.SNOWY, BiomeDictionary.Type.FOREST, BiomeDictionary.Type.DENSE);
        BiomeDictionary.addTypes(HVERGELMIR_THROAT, BiomeDictionary.Type.COLD, BiomeDictionary.Type.SNOWY, BiomeDictionary.Type.WASTELAND, BiomeDictionary.Type.MAGICAL);
    }

    public static Biome[] all() {
        return new Biome[] {
                MISTVEIL_DEPTHS,
                HOWLING_ICEFIELD,
                SUNKEN_TEAR,
                RIMEFANG_MOUNTAINS,
                SOUL_EMBER_PLAINS,
                GLIMMERING_HOLLOW,
                HVERGELMIR_THROAT
        };
    }
}
