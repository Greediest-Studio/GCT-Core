package com.smd.gctcore.common.config;

import com.cleanroommc.configanytime.ConfigAnytime;
import com.smd.gctcore.Tags;
import com.smd.gctcore.common.world.jotunheim.JotunheimTerrainKernel;
import net.minecraftforge.common.config.Config;

/** Generation settings are snapshotted when a world provider is opened. */
@Config(modid = Tags.MOD_ID, name = "GCTCore/Jotunheim")
public final class JotunheimConfig {
    @Config.Comment("One in this many otherwise valid giant natural-spawn attempts succeeds. Lower values spawn more giants; independent of species weights.")
    @Config.RangeInt(min = 1, max = 128)
    public static int giantSpawnRoll = 24;

    @Config.Comment("Maximum living giants within 64 horizontal blocks and 24 vertical blocks of a natural-spawn candidate. Does not restrict eggs, commands or spawners.")
    @Config.RangeInt(min = 1, max = 8)
    public static int giantLocalLimit = 2;

    @Config.Comment("Base XZ biome region scale in blocks. Island coherence adds 160 * islandScale blocks. Restart the world; only new chunks change.")
    @Config.RangeDouble(min = 256, max = 4096)
    @Config.RequiresWorldRestart
    public static double biomeScale = 640.0D;

    @Config.Comment("Island occurrence and coverage multiplier. Restart the world; only new chunks change.")
    @Config.RangeDouble(min = 0.55D, max = 1.4D)
    @Config.RequiresWorldRestart
    public static double islandDensity = 1.0D;

    @Config.Comment("Horizontal island size and spacing multiplier. Does not change layer height bands.")
    @Config.RangeDouble(min = 0.65D, max = 1.75D)
    @Config.RequiresWorldRestart
    public static double islandScale = 1.0D;

    @Config.Comment("Island body and downward root thickness multiplier, bounded inside each height band. Does not raise island surfaces.")
    @Config.RangeDouble(min = 0.55D, max = 1.5D)
    @Config.RequiresWorldRestart
    public static double thickness = 1.0D;

    @Config.Comment("Natural surface relief multiplier. Cloud terrain reserves at least 18 blocks for future trees.")
    @Config.RangeDouble(min = 0.25D, max = 1.75D)
    @Config.RequiresWorldRestart
    public static double heightVariation = 1.0D;

    @Config.Comment("Three-dimensional edge/surface erosion strength. Zero disables fine erosion.")
    @Config.RangeDouble(min = 0.0D, max = 1.6D)
    @Config.RequiresWorldRestart
    public static double erosion = 0.85D;

    @Config.Comment("Deep island tunnel width multiplier. Zero disables deep tunnel carving.")
    @Config.RangeDouble(min = 0.0D, max = 1.5D)
    @Config.RequiresWorldRestart
    public static double caveAmount = 1.0D;

    static {
        ConfigAnytime.register(JotunheimConfig.class);
    }

    private JotunheimConfig() {}

    public static JotunheimTerrainKernel.Settings snapshot() {
        return new JotunheimTerrainKernel.Settings(biomeScale, islandDensity, islandScale,
                thickness, heightVariation, erosion, caveAmount);
    }
}
