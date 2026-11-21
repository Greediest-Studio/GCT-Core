package com.smd.gctcore.common.world.biome.jotunheim;

import com.smd.gctcore.Tags;
import com.smd.gctcore.misc.JotunheimBlocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.biome.Biome;

/** Ordinary XZ biome identity; vertical ecological identity belongs to JotunheimEcology. */
public final class BiomeJotunheimBase extends Biome {
    private final int index;
    private final String translationKey;
    private final int fogColor;
    private final int grassColor;
    private final int foliageColor;

    public BiomeJotunheimBase(int index, String path, String displayName, String top, String filler,
                             float temperature, float rainfall, int fogColor, int grassColor, int foliageColor) {
        super(new BiomeProperties(displayName).setBaseHeight(0.1F).setHeightVariation(0.2F)
                .setTemperature(temperature).setRainfall(rainfall).setRainDisabled());
        this.index = index;
        this.translationKey = "biome.gctcore." + path + ".name";
        this.fogColor = fogColor;
        this.grassColor = grassColor;
        this.foliageColor = foliageColor;
        setRegistryName(new ResourceLocation(Tags.MOD_ID, path));
        this.topBlock = JotunheimBlocks.state(top);
        this.fillerBlock = JotunheimBlocks.state(filler);
        this.spawnableMonsterList.clear();
        this.spawnableCreatureList.clear();
        this.spawnableWaterCreatureList.clear();
        this.spawnableCaveCreatureList.clear();
        this.decorator.treesPerChunk = -999;
        this.decorator.flowersPerChunk = -999;
        this.decorator.grassPerChunk = -999;
        this.decorator.generateFalls = false;
    }

    public int index() { return index; }
    public int getFogColor() { return fogColor; }
    public int getGrassColor() { return grassColor; }
    public int getFoliageColor() { return foliageColor; }

    /** Vanilla's getBiomeName is final in 1.12; this is available to custom UI. */
    public String getLocalizedName() {
        return I18n.canTranslate(translationKey) ? I18n.translateToLocal(translationKey) : super.getBiomeName();
    }

    @Override
    public int getGrassColorAtPos(BlockPos pos) { return grassColor; }

    @Override
    public int getFoliageColorAtPos(BlockPos pos) { return foliageColor; }
}
