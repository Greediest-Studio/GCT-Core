package com.gmm.gctold.common.world.biome;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.RegistryEvent;

public final class GctAllBiomes {
    private GctAllBiomes() {
    }

    public static void registerBiomes(RegistryEvent.Register<Biome> event) {
        event.getRegistry().registerAll(
                BiomeDarkerRealm.biome,
                BiomeWarped.biome,
                BiomeBloodyplain.biome,
                BiomeCurruptplain.biome,
                BiomeManaForest.biome,
                BiomeEverheavenWaste.biome,
                BiomeAlfheimPlain.biome,
                BiomeManaForestHill.biome,
                BiomeAlfheimPlainHill.biome,
                BiomeAlfheimDesert.biome,
                BiomeAlfheimDesertHill.biome,
                BiomeAlfheimDesertRichland.biome,
                BiomeAlfheimForest.biome,
                BiomeReversedForest.biome,
                BiomeAriesForest.biome,
                BiomeTaurusPlataeu.biome,
                BiomeGeminiValley.biome,
                BiomeCancerHills.biome,
                BiomeLeoBadlands.biome,
                BiomeVirgoPlain.biome,
                BiomeCapricornsCliffs.biome,
                BiomeAquariusIceplain.biome,
                BiomePiscesOcean.biome,
                BiomeSagittariusDenseforest.biome,
                BiomeScorpioDesert.biome,
                BiomeLibraIsland.biome,
                BiomeVoidPlain.biome,
                BiomeVoidHill.biome,
                BiomeNowhere.biome,
                BiomeOrderPlain.biome,
                BiomeOrderPlateu.biome,
                BiomeOrderBasin.biome
        );
    }

    public static void init() {
        BiomeDictionary.addTypes(BiomeDarkerRealm.biome, BiomeDictionary.Type.PLAINS, BiomeDictionary.Type.DRY);
        BiomeDictionary.addTypes(BiomeWarped.biome, BiomeDictionary.Type.HOT, BiomeDictionary.Type.PLAINS,
                BiomeDictionary.Type.SAVANNA, BiomeDictionary.Type.DRY);
        BiomeDictionary.addTypes(BiomeBloodyplain.biome, BiomeDictionary.Type.WASTELAND);
        BiomeDictionary.addTypes(BiomeCurruptplain.biome, BiomeDictionary.Type.WASTELAND);
        BiomeDictionary.addTypes(BiomeManaForest.biome, BiomeDictionary.Type.FOREST);
        BiomeDictionary.addTypes(BiomeEverheavenWaste.biome, BiomeDictionary.Type.WASTELAND);
        BiomeDictionary.addTypes(BiomeAlfheimPlain.biome, BiomeDictionary.Type.PLAINS, BiomeDictionary.Type.MAGICAL);
        BiomeDictionary.addTypes(BiomeManaForestHill.biome, BiomeDictionary.Type.FOREST);
        BiomeDictionary.addTypes(BiomeAlfheimPlainHill.biome, BiomeDictionary.Type.PLAINS, BiomeDictionary.Type.MAGICAL);
        BiomeDictionary.addTypes(BiomeAlfheimDesert.biome, BiomeDictionary.Type.SANDY, BiomeDictionary.Type.DRY);
        BiomeDictionary.addTypes(BiomeAlfheimDesertHill.biome, BiomeDictionary.Type.SANDY, BiomeDictionary.Type.DRY);
        BiomeDictionary.addTypes(BiomeAlfheimDesertRichland.biome, BiomeDictionary.Type.SANDY, BiomeDictionary.Type.DRY);
        BiomeDictionary.addTypes(BiomeAlfheimForest.biome, BiomeDictionary.Type.MAGICAL, BiomeDictionary.Type.FOREST);
        BiomeDictionary.addTypes(BiomeReversedForest.biome, BiomeDictionary.Type.FOREST,
                BiomeDictionary.Type.MUSHROOM, BiomeDictionary.Type.MAGICAL);
        BiomeDictionary.addTypes(BiomeAriesForest.biome, BiomeDictionary.Type.FOREST);
        BiomeDictionary.addTypes(BiomeCancerHills.biome, BiomeDictionary.Type.HILLS);
        BiomeDictionary.addTypes(BiomeLeoBadlands.biome, BiomeDictionary.Type.WASTELAND);
        BiomeDictionary.addTypes(BiomeVirgoPlain.biome, BiomeDictionary.Type.PLAINS);
        BiomeDictionary.addTypes(BiomeCapricornsCliffs.biome, BiomeDictionary.Type.MOUNTAIN);
        BiomeDictionary.addTypes(BiomeAquariusIceplain.biome, BiomeDictionary.Type.PLAINS, BiomeDictionary.Type.COLD);
        BiomeDictionary.addTypes(BiomePiscesOcean.biome, BiomeDictionary.Type.OCEAN);
        BiomeDictionary.addTypes(BiomeSagittariusDenseforest.biome, BiomeDictionary.Type.FOREST);
        BiomeDictionary.addTypes(BiomeScorpioDesert.biome, BiomeDictionary.Type.WASTELAND);
        BiomeDictionary.addTypes(BiomeLibraIsland.biome, BiomeDictionary.Type.WASTELAND);
        BiomeVoidPlain.init();
        BiomeVoidHill.init();
        BiomeNowhere.init();
    }
}
