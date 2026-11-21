package com.smd.gctcore.misc;

import com.smd.gctcore.common.blocks.jotunheim.BlockJotunheimAercloud;
import com.smd.gctcore.common.blocks.jotunheim.BlockJotunheimLeaves;
import com.smd.gctcore.common.blocks.jotunheim.BlockJotunheimLog;
import com.smd.gctcore.common.blocks.jotunheim.BlockJotunheimMoss;
import com.smd.gctcore.common.blocks.jotunheim.BlockJotunheimNatural;
import com.smd.gctcore.common.blocks.jotunheim.BlockJotunheimPlant;
import com.smd.gctcore.common.blocks.jotunheim.BlockJotunheimReed;
import com.smd.gctcore.common.blocks.jotunheim.BlockJotunheimSand;
import com.smd.gctcore.common.blocks.jotunheim.BlockJotunheimSoil;
import com.smd.gctcore.common.blocks.jotunheim.BlockJotunheimSteam;
import com.smd.gctcore.common.blocks.jotunheim.BlockJotunheimVine;
import com.smd.gctcore.common.blocks.jotunheim.ItemBlockJotunheimAercloud;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Static natural-block registry, mirrored by the Jotunheim art manifests. */
public final class JotunheimBlocks {
    public static final class Entry {
        private final String key;
        private final IBlockState state;
        private Entry(String key, IBlockState state) { this.key = key; this.state = state; }
        public String getKey() { return key; }
        public Block getBlock() { return state.getBlock(); }
        public IBlockState getState() { return state; }
        public String getModelPath() { return "jotunheim/" + key; }
        public int getMetadata() { return getBlock().getMetaFromState(state); }
    }

    private static final Map<String, Entry> ENTRIES = new LinkedHashMap<>();
    private static final List<Block> BLOCKS = new ArrayList<>();
    private static final ResourceLocation REMOVED_CLOUDROCK =
            new ResourceLocation("gctcore", "jotunheim_luminous_cloudrock");

    public static void init() {
        if (!ENTRIES.isEmpty()) return;
        soil("jotun_dirt", "jotunheim_dirt", false, 0);
        soil("jotun_grass", "jotunheim_grass", true, 0);
        soil("jotun_permafrost", "jotunheim_permafrost", false, 0);
        soil("humus", "jotunheim_humus", false, 0);
        soil("jotunn_moss", "jotunheim_moss", true, 0);
        add("rotten_moss", new BlockJotunheimMoss("jotunheim_rotten_moss"), 0);
        soil("luminous_moss", "jotunheim_luminous_moss", true, 5);
        soil("volcanic_ash", "jotunheim_volcanic_ash", false, 0);
        add("steam", new BlockJotunheimSteam("jotunheim_steam"), 0);

        log("jotunn_oak_log", "jotunheim_oak_log");
        bark("jotunn_oak_bark", "jotunheim_oak_bark");
        leaves("jotunn_oak_leaves", "jotunheim_oak_leaves", "jotunn_oak_sapling");
        plant("jotunn_oak_sapling", "jotunheim_oak_sapling", false);
        log("elder_pine_log", "jotunheim_elder_pine_log");
        bark("elder_pine_bark", "jotunheim_elder_pine_bark");
        leaves("elder_pine_leaves", "jotunheim_elder_pine_leaves", "elder_pine_sapling");
        plant("elder_pine_sapling", "jotunheim_elder_pine_sapling", false);
        log("sunkenwood_log", "jotunheim_sunkenwood_log");
        bark("sunkenwood_bark", "jotunheim_sunkenwood_bark");
        leaves("sunkenwood_leaves", "jotunheim_sunkenwood_leaves", "sunkenwood_sapling");
        plant("sunkenwood_sapling", "jotunheim_sunkenwood_sapling", false);
        leaves("white_crown_leaves", "jotunheim_white_crown_leaves", "jotunn_oak_sapling");
        log("charred_log", "jotunheim_charred_log");
        log("lightning_struck_log", "jotunheim_lightning_struck_log");
        log("ancient_root", "jotunheim_ancient_root");
        log("fungal_stem", "jotunheim_fungal_stem");
        fungus("luminous_fungal_block", "jotunheim_luminous_fungal_block", 7);
        fungus("ember_fungus_block", "jotunheim_ember_fungus_block", 5);
        add("cloudvine", new BlockJotunheimVine("jotunheim_cloudvine"), 7);
        add("mistvine", new BlockJotunheimVine("jotunheim_mistvine"), 8);
        add("giant_reed", new BlockJotunheimReed("jotunheim_giant_reed"), 5);

        // Most small flora emits actual block light, with softer stems and brighter flowers/fungi.
        plant("cloud_bellflower", "jotunheim_cloud_bellflower", false, 10);
        plant("rootshade_fern", "jotunheim_rootshade_fern", false, 7);
        plant("frost_cup_flower", "jotunheim_frost_cup_flower", true, 9);
        plant("silverneedle_grass", "jotunheim_silverneedle_grass", true, 6);
        plant("crag_blossom", "jotunheim_crag_blossom", true, 7);
        plant("giant_seedgrass", "jotunheim_giant_seedgrass", false);
        plant("fen_lantern_sedge", "jotunheim_fen_lantern_sedge", false, 12);
        plant("mistcup_fungus", "jotunheim_mistcup_fungus", false, 10);
        plant("crystal_fern", "jotunheim_crystal_fern", true, 9);
        plant("violet_starflower", "jotunheim_violet_starflower", true, 10);
        plant("thunderwhisker_grass", "jotunheim_thunderwhisker_grass", true, 10);
        plant("windwool_flower", "jotunheim_windwool_flower", true, 8);
        plant("ash_fern", "jotunheim_ash_fern", true);
        plant("ember_mushroom", "jotunheim_ember_mushroom", true, 11);
        plant("rune_lichen", "jotunheim_rune_lichen", true, 8);
        plant("pale_droopflower", "jotunheim_pale_droopflower", false, 11);

        stone("cloudrock", "jotunheim_cloudrock", 1.5F);
        stone("giantstone", "jotunheim_giantstone", 2.5F);
        stone("weathered_giantstone", "jotunheim_weathered_giantstone", 2.0F);
        stone("volcanic_rock", "jotunheim_volcanic_rock", 2.5F);
        stone("heated_rock", "jotunheim_heated_rock", 2.8F);
        get("heated_rock").setLightLevel(9.0F / 15.0F);
        stone("obsidian_rock", "jotunheim_obsidian_rock", 5.0F);
        stone("thunderstone", "jotunheim_thunderstone", 2.5F);
        stone("windworn_stone", "jotunheim_windworn_stone", 1.8F);
        transparent("ancient_ice", "jotunheim_ancient_ice", Material.ICE, false);
        transparent("deep_ice_crystal", "jotunheim_deep_ice_crystal", Material.ICE, false);
        stone("violet_crystal_stone", "jotunheim_violet_crystal_stone", 2.5F);
        stone("azure_crystal_stone", "jotunheim_azure_crystal_stone", 2.5F);
        stone("crimson_crystal_stone", "jotunheim_crimson_crystal_stone", 2.5F);
        add("crystal_sand", new BlockJotunheimSand("jotunheim_crystal_sand"), 0);
        transparent("clear_crystal", "jotunheim_clear_crystal", Material.GLASS, false);
        transparent("natural_crystal", "jotunheim_natural_crystal", Material.GLASS, true);
        transparent("ice_spike", "jotunheim_ice_spike", Material.ICE, true);

        BlockJotunheimAercloud clouds = new BlockJotunheimAercloud();
        BLOCKS.add(clouds);
        for (BlockJotunheimAercloud.Variant variant : BlockJotunheimAercloud.Variant.values()) {
            String key = variant.getName() + "_aercloud";
            ENTRIES.put(key, new Entry(key, clouds.getDefaultState().withProperty(BlockJotunheimAercloud.VARIANT, variant)));
        }
    }

    private static void add(String key, Block block, int light) {
        block.setLightLevel(light / 15.0F);
        BLOCKS.add(block);
        ENTRIES.put(key, new Entry(key, block.getDefaultState()));
    }
    private static void soil(String key, String name, boolean grass, int light) {
        add(key, new BlockJotunheimSoil(name, grass), light);
    }
    private static void log(String key, String name) { add(key, new BlockJotunheimLog(name), 0); }
    private static void bark(String key, String name) {
        add(key, new BlockJotunheimNatural(name, Material.WOOD, SoundType.WOOD,
                "axe", 2.0F, BlockRenderLayer.SOLID, false), 0);
    }
    private static void leaves(String key, String name, String sapling) {
        add(key, new BlockJotunheimLeaves(name, sapling), 0);
    }
    private static void fungus(String key, String name, int light) {
        add(key, new BlockJotunheimNatural(name, Material.WOOD, SoundType.WOOD,
                "axe", 0.4F, BlockRenderLayer.SOLID, false), light);
    }
    private static void plant(String key, String name, boolean rockPlant) {
        plant(key, name, rockPlant, 0);
    }
    private static void plant(String key, String name, boolean rockPlant, int light) {
        add(key, new BlockJotunheimPlant(name, rockPlant, "rune_lichen".equals(key)), light);
    }
    private static void stone(String key, String name, float hardness) {
        Block block = new BlockJotunheimNatural(name, Material.ROCK, SoundType.STONE,
                "pickaxe", hardness, BlockRenderLayer.SOLID, false);
        block.setHarvestLevel("pickaxe", 13);
        add(key, block, 0);
    }
    private static void transparent(String key, String name, Material material, boolean cluster) {
        add(key, new BlockJotunheimNatural(name, material, SoundType.GLASS,
                "pickaxe", 0.8F, BlockRenderLayer.TRANSLUCENT, cluster), 0);
    }

    public static Block get(String key) { return state(key).getBlock(); }
    public static IBlockState state(String key) {
        Entry entry = ENTRIES.get(key);
        if (entry == null) throw new IllegalArgumentException("Unknown Jotunheim block key: " + key);
        return entry.getState();
    }
    public static List<Entry> entries() {
        return Collections.unmodifiableList(new ArrayList<>(ENTRIES.values()));
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        for (Block block : BLOCKS) event.getRegistry().register(block);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        for (Block block : BLOCKS) {
            ItemBlock item = block instanceof BlockJotunheimAercloud
                    ? new ItemBlockJotunheimAercloud((BlockJotunheimAercloud) block) : new ItemBlock(block);
            item.setRegistryName(block.getRegistryName());
            event.getRegistry().register(item);
        }
    }

    @SubscribeEvent
    public void remapRemovedBlocks(RegistryEvent.MissingMappings<Block> event) {
        for (RegistryEvent.MissingMappings.Mapping<Block> mapping : event.getAllMappings()) {
            if (REMOVED_CLOUDROCK.equals(mapping.key)) mapping.remap(get("cloudrock"));
        }
    }

    @SubscribeEvent
    public void remapRemovedItems(RegistryEvent.MissingMappings<Item> event) {
        for (RegistryEvent.MissingMappings.Mapping<Item> mapping : event.getAllMappings()) {
            if (REMOVED_CLOUDROCK.equals(mapping.key)) {
                mapping.remap(Item.getItemFromBlock(get("cloudrock")));
            }
        }
    }
}
