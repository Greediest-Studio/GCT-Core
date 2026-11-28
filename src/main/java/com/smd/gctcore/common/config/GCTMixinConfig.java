package com.smd.gctcore.common.config;

import com.cleanroommc.configanytime.ConfigAnytime;
import com.smd.gctcore.Tags;
import net.minecraftforge.common.config.Config;

@Config(modid = Tags.MOD_ID, name = "GCTCore/GCTMixin")
public class GCTMixinConfig {

    @Config.Comment({"是否使用局部名称索引加速 GeckoLib 骨架建树"})
    @Config.Name("Enable GeckoLib Hierarchy Index")
    @Config.RequiresMcRestart
    public static boolean enableGeckoHierarchyIndex = true;

    @Config.Comment({"是否避免 NuclearCraft 附加包对已有 ContentTweaker 资源的重复备份和覆盖"})
    @Config.Name("Enable NuclearCraft Resource Merge")
    @Config.RequiresMcRestart
    public static boolean enableNuclearCraftResourceMerge = true;

    @Config.Comment({"是否跳过 NuclearCraft 在 postInit 阶段自动导出 config/nuclearcraft.ncpf.json",
            "该文件仅供外部反应堆规划器使用，游戏运行时不会读取；跳过可减少约 1 秒启动耗时",
            "需要重新生成时可临时关闭本项，或添加 JVM 参数 -Dgctcore.exportNcpf=true"})
    @Config.Name("Disable NuclearCraft NCPF Auto Export")
    @Config.RequiresMcRestart
    public static boolean disableNuclearCraftNCPFExport = true;

    @Config.Comment({"是否优化 CustomLoadingScreen 在 construct 阶段的资源重载",
            "1) 跳过 MinecraftDisplayerRenderer 构造函数中重复触发的第二次全量 refreshResources()",
            "2) 在 ClsManager.load() 首次刷新资源包时跳过耗时的全模组 LanguageManager 语言文件重载（留待后续 FML 初始化统一加载）"})
    @Config.Name("Enable CustomLoadingScreen Resource Reload Optimization")
    @Config.RequiresMcRestart
    public static boolean enableCustomLoadingScreenResourceReloadOptimization = true;

    @Config.Comment({"是否缓存 JAOPCA 的资源存在性检查结果，避免流体渲染时反复查询纹理文件",
            "同时缓存不存在的资源，资源包重载开始时自动清空"})
    @Config.Name("Enable JAOPCA Resource Cache")
    @Config.RequiresMcRestart
    public static boolean enableJAOPCAResourceCache = true;

    @Config.Comment({"是否避免 InGame Info XML 图标缓存命中时深拷贝物品 NBT",
            "仅在新缓存键写入时保存独立 NBT 快照，保留数量、耐久和图标叠层状态区分"})
    @Config.Name("Enable IGI Item Cache Optimization")
    @Config.RequiresMcRestart
    public static boolean enableIGIItemCacheOptimization = true;

    @Config.Comment({"是否修复 InGame Info XML CanoeEdition 3.0.0.20 的 HUD 行重复入队",
            "每行只保留一次，避免文字和物品图标重复绘制"})
    @Config.Name("Enable IGI Duplicate Overlay Fix")
    @Config.RequiresMcRestart
    public static boolean enableIGIDuplicateOverlayFix = true;

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

    @Config.Comment({"是否在 PreInit 阶段合并多次 Minecraft / FML 资源包重载请求，并在 PreInit 结束后统一执行一次",
            "可避免 Additions、Quark、AquaAcrobatics 等模组触发多次全量或部分资源重刷与 Patchouli 重复解析"})
    @Config.Name("Enable PreInit Resource Reload Coalescing")
    @Config.RequiresMcRestart
    public static boolean enablePreInitResourceReloadCoalescing = true;

    @Config.Comment({"是否将 Kirino ECS 的 ClassGraph 注解扫描限定到实际包含目标包的类路径来源（仅 Cleanroom）",
            "避免 CleanECSRuntime 初始化阶段用 68 个工作线程枚举并读取全部模组 JAR"})
    @Config.Name("Enable Kirino ECS Scan Optimization")
    @Config.RequiresMcRestart
    public static boolean enableKirinoECSScanOptimization = true;

    @Config.Comment({"是否修复失落城市 (Lost Cities) 在 REID/JEID 环境下因 16 位 char 方块状态 ID 截断导致的建筑方块错乱或消失"})
    @Config.Name("Enable LostCities REID BlockState Fix")
    @Config.RequiresMcRestart
    public static boolean enableLostCitiesREIDFix = true;

    @Config.Comment({"是否缓存 NetherEx PigtificateVillageManager 中不存在村庄数据的维度",
            "避免主世界、末地等无村庄数据的维度在每个服务端 tick 都反复执行磁盘 File.exists() 检查"})
    @Config.Name("Enable NetherEx Village Data Negative Cache")
    @Config.RequiresMcRestart
    public static boolean enableNetherExVillageDataNegativeCache = true;

    static {
        ConfigAnytime.register(GCTMixinConfig.class);
    }
}
