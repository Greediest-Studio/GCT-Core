package com.smd.gctcore.common.config;

import com.cleanroommc.configanytime.ConfigAnytime;
import com.smd.gctcore.Tags;
import net.minecraftforge.common.config.Config;

@Config(modid = Tags.MOD_ID, name = "GCTCore/GCTMixin")
public class GCTMixinConfig {

    @Config.Comment({"是否优化 JEI 启动时 TComp 覆盖配方匹配及 TE 容器流体枚举"})
    @Config.Name("Enable JEI Startup Optimizations")
    @Config.RequiresMcRestart
    public static boolean enableJEIStartupOptimizations = true;

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

    @Config.Comment({"是否限制区块渲染器 (ChunkRenderDispatcher) 缓冲池数量上限",
            "可避免 LoliASM 10 倍池化与 Lumenized Bloom 渲染层叠加导致堆外原生内存暴涨"})
    @Config.Name("Enable Chunk Render Builder Cap")
    @Config.RequiresMcRestart
    public static boolean enableChunkRenderBuilderCap = true;

    @Config.Comment({"区块渲染器 (ChunkRenderDispatcher) 缓冲池数量上限",
            "仅在启用限制且原始池大小超过该值时生效；默认 20 可兼顾高视距缓冲余量与堆外内存节省"})
    @Config.Name("Max Chunk Render Builders")
    @Config.RangeInt(min = 1, max = 256)
    @Config.RequiresMcRestart
    public static int maxChunkRenderBuilders = 20;

    @Config.Comment({"是否优化 AE2 普通及无线合成终端的连续合成，包含多态合成兼容"})
    @Config.Name("Enable AE2 Crafting Recipe Cache")
    @Config.RequiresMcRestart
    public static boolean enableAE2CraftingRecipeCache = true;

    @Config.Comment({"是否禁止 HammerLib 启动时联网拉取链接仓库",
            "关闭后不再请求 pastebin/gist/zeith.org，避免无网络或类加载异常导致启动崩溃"})
    @Config.Name("Disable HammerLib Link Repository Fetch")
    @Config.RequiresMcRestart
    public static boolean disableHammerLibLinkRepository = true;

    @Config.Comment({"是否在 CompactMachines3 postInit 完成后释放 AnnotatedInstanceUtil.asmData",
            "CompactMachines3 仅在初始化阶段读取该 ASM 扫描表，清理后可释放约百 MiB 堆内存"})
    @Config.Name("Enable CompactMachines3 ASMData Cleanup")
    @Config.RequiresMcRestart
    public static boolean enableCompactMachinesASMDataCleanup = true;

    @Config.Comment({"是否将星辉魔法 (AstralSorcery) 技能树界面渲染缓冲改为按需懒加载并在关闭界面时释放",
            "避免游戏启动后永久占用约 64 MiB 堆外直接内存"})
    @Config.Name("Enable AstralSorcery PerkTree Lazy Buffers")
    @Config.RequiresMcRestart
    public static boolean enableAstralPerkTreeLazyBuffers = true;

    @Config.Comment({"是否将 BetterNether 下界城市结构管理器 (BNWorldGenerator.cityManager) 改为按需懒加载与软引用缓存",
            "未进入下界时不构建，离开下界或世界卸载时保存并释放内存"})
    @Config.Name("Enable BetterNether City Lazy Cache")
    @Config.RequiresMcRestart
    public static boolean enableBetterNetherCityLazyCache = true;

    @Config.Comment({"是否将原版 StatList / StatCrafting 的物品名称与 HoverEvent 悬浮文本改为按需懒构造",
            "避免初始化阶段为全物品统计项预生成 NBTTagCompound、Style 与 TextComponent 树而长期占用约 70–85 MiB 堆内存"})
    @Config.Name("Enable Lazy Stat Names")
    @Config.RequiresMcRestart
    public static boolean enableLazyStatNames = true;

    static {
        ConfigAnytime.register(GCTMixinConfig.class);
    }
}
