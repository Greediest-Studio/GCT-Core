package com.smd.gctcore.common.config;

import com.smd.gctcore.Tags;
import com.smd.gctcore.misc.moretcon.BedrockBlockChecker;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Tags.MOD_ID)
@Config.LangKey("gctcore.config.title")
public class GctCoreConfig {

    @Config.Comment("Integration with MoreTcon mod")
    @Config.Name("Moartcon Integration")
    public static MoreTconIntegration moreTconIntegration = new MoreTconIntegration();

    @Config.Comment("Radiant resonator settings")
    @Config.Name("Radiant Resonator")
    public static RadiantResonator radiantResonator = new RadiantResonator();

    @Config.Comment("Debug tools for MMCE controller state changes")
    @Config.Name("MMCE Debug")
    public static MMCE_Debug mmceDebug = new MMCE_Debug();

    public static class RadiantResonator {

        @Config.Comment("Maximum radiant resonators each player can place")
        @Config.Name("Resonator Limit")
        @Config.RangeInt(min = 1, max = 1024)
        public int resonatorLimit = 3;

        @Config.Comment("Ticks required to generate one raw quartz cluster")
        @Config.Name("Resonator Tick Time")
        @Config.RangeInt(min = 1)
        public int resonatorTickTime = 6000;
    }

    public static class MMCE_Debug {

        @Config.Comment({
                "Print MMCE controller state changes from the server tick event.",
                "Use this to see formed/status/facing changes over time."
        })
        @Config.Name("Log Controller Tick State Changes")
        public boolean logControllerTickStateChanges = true;

        @Config.Comment({
                "Print a stack trace when World#setBlockState changes an MMCE controller FACING or FORMED property.",
                "Enable only while debugging; it can be noisy."
        })
        @Config.Name("Trace Controller Block State Changes")
        public boolean traceControllerBlockStateChanges = true;

        @Config.Comment("Maximum stack trace elements printed for MMCE controller block state changes.")
        @Config.Name("Trace Stack Depth")
        @Config.RangeInt(min = 4, max = 80)
        public int traceStackDepth = 30;
    }

    public static class MoreTconIntegration {
        
        @Config.Comment({
            "List of blocks that should act as bedrock-like blocks (requiring BottomsEnd trait to mine).",
            "Format: modid:blockid@metadata[:soft|:hard]",
            "Examples:",
            "  minecraft:stone@0",
            "  minecraft:obsidian@0",
            "  tconstruct:seared@0",
            "Metadata is optional. If omitted, all metadata values will match.",
            "Use * as wildcard for metadata to match all variants: minecraft:wool@*",
            "Append :soft or :hard to force per-entry soft/hard behavior: minecraft:obsidian@0:hard"
        })
        @Config.Name("Bedrock-like Blocks")
        public String[] bedrockLikeBlocks = new String[] {
            "minecraft:obsidian@0"
        };

        @Config.Comment({
            "If true, blocks in the bedrock-like list will be treated as 'soft bedrock'.",
            "Soft bedrock mines faster than regular bedrock with BottomsEnd tools."
        })
        @Config.Name("Treat as Soft Bedrock")
        public boolean treatAsSoftBedrock = true;
    }

    @Config.Comment("Smeltery accelerator integration (replaces SimpleSmelteryAccelerator)")
    @Config.Name("Smeltery Integration")
    public static SmelteryIntegration smelteryIntegration = new SmelteryIntegration();

    public static class SmelteryIntegration {

        @Config.Comment({
                "Multiplier for TileSmeltery processing speed.",
        })
        @Config.Name("Smeltery Multiplier")
        @Config.RangeInt(min = 1, max = 100)
        public int smelteryMultiplier = 4;
    }



    @Config.Comment("AbyssalCraft")
    @Config.Name("AbyssalCraft")
    public static AbyssalCraft abyssalCraftIntegration = new AbyssalCraft();

    public static class AbyssalCraft {

        @Config.Comment({
                "Enable Oblivion Catalyst Effects"
        })
        @Config.Name("Enable Oblivion Catalyst Effects")
        public boolean enableOblivionCatalystEffects = true;
    }

    @Config.Comment("Astral Sorcery integration settings")
    @Config.Name("Astral Sorcery")
    public static AstralSorceryIntegration astralSorceryIntegration = new AstralSorceryIntegration();

    public static class AstralSorceryIntegration {

        @Config.Comment({
                "调整允许的最大技能效率，基础值为1，-1默认为不修改，"
        })
        @Config.Name("最大技能效率")
        @Config.RangeDouble(min = -1, max = 10)
        public double maxPerkEffect = -1;
    }

    @Mod.EventBusSubscriber(modid = Tags.MOD_ID)
    private static class EventHandler {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(Tags.MOD_ID)) {
                ConfigManager.sync(Tags.MOD_ID, Config.Type.INSTANCE);
                // Refresh bedrock block checker cache when config changes
                BedrockBlockChecker.markDirty();
            }
        }
    }
}
