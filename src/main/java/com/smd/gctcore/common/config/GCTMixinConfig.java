package com.smd.gctcore.common.config;

import com.cleanroommc.configanytime.ConfigAnytime;
import com.smd.gctcore.Tags;
import net.minecraftforge.common.config.Config;

@Config(modid = Tags.MOD_ID, name = "GCTCore/GCTMixin")
public class GCTMixinConfig {

    @Config.Comment({"是否禁用匠魂进化法杖右键的投射物属性"})
    @Config.Name("Enable Mixin")
    @Config.RequiresMcRestart
    public static boolean enableMixinItemToolSceptre = true;

    static {
        ConfigAnytime.register(GCTMixinConfig.class);
    }
}
