package com.smd.gctcore.common.config;

import com.cleanroommc.configanytime.ConfigAnytime;
import com.smd.gctcore.Tags;
import com.smd.gctcore.misc.moretcon.BedrockBlockChecker;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = Tags.MOD_ID, name = "GCTCore/GCTCompat")
@Config.LangKey("gctcore.config.title")
public class GCTCompatConfig {

    @Config.Comment("Integration with MoreTcon mod")
    @Config.Name("Moartcon Integration")
    public static MoreTconIntegration moreTconIntegration = new MoreTconIntegration();

    @Config.Comment("Radiant resonator settings")
    @Config.Name("Radiant Resonator")
    public static RadiantResonator radiantResonator = new RadiantResonator();

    @Config.Comment("MaterialShaderFix settings")
    @Config.Name("MaterialShaderFix")
    public static MaterialShaderFix materialShaderFix = new MaterialShaderFix();

    @Config.Comment("Smeltery accelerator integration (replaces SimpleSmelteryAccelerator)")
    @Config.Name("Smeltery Integration")
    public static SmelteryIntegration smelteryIntegration = new SmelteryIntegration();

    @Config.Comment("AbyssalCraft")
    @Config.Name("AbyssalCraft")
    public static AbyssalCraft abyssalCraftIntegration = new AbyssalCraft();

    @Config.Comment("Astral Sorcery integration settings")
    @Config.Name("Astral Sorcery")
    public static AstralSorceryIntegration astralSorceryIntegration = new AstralSorceryIntegration();

    @Config.Comment("Mekanism CE balance patches (laser / digital miner harvest limits)")
    @Config.Name("Mekanism Integration")
    public static MekanismIntegration mekanismIntegration = new MekanismIntegration();

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

    public static class MaterialShaderFix {

        @Config.Comment({
                "Enable the material shader fix for custom part types (laser_medium, battery_cell, tconevo.magic).",
                "When enabled, materials with standard stats will render correctly on custom parts,",
                "even if they don't have the custom stat types.",
                "Disable this if you experience compatibility issues.",
                "Note: Changing this requires a game restart to take effect."
        })
        @Config.Name("Enable Shader Fix")
        @Config.RequiresMcRestart
        public boolean enableShaderFix = true;

        @Config.Comment({
                "Enable detailed logging of which materials benefit from the shader fix.",
                "Useful for debugging but can spam the log during startup."
        })
        @Config.Name("Enable Debug Logging")
        @Config.RequiresMcRestart
        public boolean enableDebugLogging = true;

        @Config.Comment({
                "Custom stat types to apply the shader fix for.",
                "Default includes PlusTiC and Tinkers-Evolution stat types.",
                "Add your own custom stat types here if needed."
        })
        @Config.Name("Custom Stat Types")
        @Config.RequiresMcRestart
        public String[] customStatTypes = {
                "laser_medium",
                "battery_cell",
                "tconevo.magic",
                "moretcon.explosive_charge"
        };

    }

    public static class SmelteryIntegration {

        @Config.Comment({
                "Multiplier for TileSmeltery processing speed.",
        })
        @Config.Name("Smeltery Multiplier")
        @Config.RangeInt(min = 1, max = 100)
        public int smelteryMultiplier = 4;
    }

    public static class AbyssalCraft {

        @Config.Comment({
                "Enable Oblivion Catalyst Effects"
        })
        @Config.Name("Enable Oblivion Catalyst Effects")
        public boolean enableOblivionCatalystEffects = true;
    }

    public static class AstralSorceryIntegration {

        @Config.Comment({
                "调整允许的最大技能效率，基础值为1，-1默认为不修改，"
        })
        @Config.Name("最大技能效率")
        @Config.RangeDouble(min = -1, max = 10)
        public double maxPerkEffect = -1;
    }

    public static class MekanismIntegration {

        @Config.Comment("Enable laser harvest-level restriction")
        @Config.Name("Enable Laser Harvest Limit")
        public boolean enableLaserHarvestLimit = true;

        @Config.Comment({
                "Maximum block harvest level lasers can break.",
                "Blocks with a higher harvest level cannot be dug by lasers.",
                "Vanilla reference: wood=0, stone=1, iron=2, diamond=3."
        })
        @Config.Name("Max Laser Harvest Level")
        @Config.RangeInt(min = -1, max = 32)
        public int maxLaserHarvestLevel = 4;

        @Config.Comment({
                "Enable Digital Miner harvest-level restriction.",
                "Mining level comes from the installed gctcore:mining_level upgrade item NBT."
        })
        @Config.Name("Enable Digital Miner Harvest Limit")
        public boolean enableDigitalMinerHarvestLimit = true;

        @Config.Comment({
                "NBT integer key on the mining-level upgrade item.",
                "Example: {gctMiningLevel:3} allows mining blocks up to harvest level 3."
        })
        @Config.Name("Mining Level NBT Key")
        public String miningLevelNbtKey = "gctMiningLevel";
    }

    static {
        ConfigAnytime.register(GCTCompatConfig.class);
    }

    @Mod.EventBusSubscriber(modid = Tags.MOD_ID)
    private static class EventHandler {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(Tags.MOD_ID)) {
                ConfigManager.sync(Tags.MOD_ID, Config.Type.INSTANCE);
                BedrockBlockChecker.markDirty();
            }
        }
    }
}
