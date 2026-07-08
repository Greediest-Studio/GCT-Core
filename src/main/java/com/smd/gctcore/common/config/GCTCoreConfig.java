package com.smd.gctcore.common.config;

import com.cleanroommc.configanytime.ConfigAnytime;
import com.smd.gctcore.Tags;
import net.minecraftforge.common.config.Config;

@Config(modid = Tags.MOD_ID, name = "GCTCore/GCTCore")
public class GCTCoreConfig {

    @Config.Comment("调整水体与熔岩的视觉清晰度")
    @Config.Name("清澈的水")
    public static CleanWater cleanwater = new CleanWater();

    public static class CleanWater {

        @Config.Comment("是否启用水下雾效调整")
        public boolean enableWater = true;

        @Config.RangeDouble(min = 0, max = 5)
        @Config.Comment("水下雾密度（0-5），原版为 0.1")
        public double fogDensityWater = 0.0f;

        @Config.Comment("是否启用熔岩下雾效调整")
        public boolean enableLava = true;

        @Config.RangeDouble(min = 0, max = 5)
        @Config.Comment("熔岩下雾密度（0-5），原版为 2.0")
        public double fogDensityLava = 0.0f;
    }

    static {
        ConfigAnytime.register(GCTCoreConfig.class);
    }
}