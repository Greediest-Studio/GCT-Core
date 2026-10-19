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

    @Config.Comment({"是否优化 AE2 普通及无线合成终端的连续合成，包含多态合成兼容"})
    @Config.Name("Enable AE2 Crafting Recipe Cache")
    @Config.RequiresMcRestart
    public static boolean enableAE2CraftingRecipeCache = true;

    @Config.Comment({"是否禁止 HammerLib 启动时联网拉取链接仓库",
            "关闭后不再请求 pastebin/gist/zeith.org，避免无网络或类加载异常导致启动崩溃"})
    @Config.Name("Disable HammerLib Link Repository Fetch")
    @Config.RequiresMcRestart
    public static boolean disableHammerLibLinkRepository = true;

    static {
        ConfigAnytime.register(GCTMixinConfig.class);
    }
}
