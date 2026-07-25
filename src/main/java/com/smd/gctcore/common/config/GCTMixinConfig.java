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

    @Config.Comment({"是否禁用伤害粒子"})
    @Config.Name("Enable Damage Particle Mixin")
    @Config.RequiresMcRestart
    public static boolean enableDamageParticleMixin = true;

    @Config.Comment({"是否启用凋零战利品表"})
    @Config.Name("Enable Wither Loot Mixin")
    @Config.RequiresMcRestart
    public static boolean enableWitherLootMixin = true;

    @Config.Comment({"是否启用区块渲染优化"})
    @Config.Name("Enable Chunk Queue Resizing Mixin")
    @Config.RequiresMcRestart
    public static boolean enableChunkQueueResizingMixin = true;

    static {
        ConfigAnytime.register(GCTMixinConfig.class);
    }
}
