package com.smd.gctcore.misc;

import com.smd.gctcore.Tags;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.LootingEnchantBonus;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraft.world.storage.loot.functions.SetMetadata;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

/** Vanilla loot tables, with optional mod items resolved after item registration. */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class GiantLootTables {
    public static final ResourceLocation CLOUD_SENTINEL = table("cloud_giant_sentinel");
    public static final ResourceLocation CLOUD_CROSSBOWMAN = table("cloud_giant_crossbowman");
    public static final ResourceLocation HILL = table("hill_giant");
    public static final ResourceLocation STONE = table("stone_giant");
    public static final ResourceLocation FOMORIAN_NOBLE = table("fomorian_noble");
    public static final ResourceLocation FOMORIAN_WARLOCK = table("fomorian_warlock");
    public static final ResourceLocation FROST = table("frost_giant");
    public static final ResourceLocation FIRE = table("fire_giant");
    public static final ResourceLocation ETTIN = table("ettin");
    private static final LootCondition[] NO_CONDITIONS = new LootCondition[0];

    private GiantLootTables() {}

    private static ResourceLocation table(String name) {
        return new ResourceLocation(Tags.MOD_ID, "entities/" + name);
    }

    public static void init() {
        for (ResourceLocation table : new ResourceLocation[] { CLOUD_SENTINEL, CLOUD_CROSSBOWMAN,
                HILL, STONE, FOMORIAN_NOBLE, FOMORIAN_WARLOCK, FROST, FIRE, ETTIN }) {
            LootTableList.register(table);
        }
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (FROST.equals(event.getName())) {
            List<LootEntry> entries = new ArrayList<>();
            addVariants(entries, "tiths:broken_ice", 1, 1, 2);
            addPool(event, "gctcore_broken_ice", entries);
        } else if (CLOUD_SENTINEL.equals(event.getName()) || CLOUD_CROSSBOWMAN.equals(event.getName())) {
            List<LootEntry> entries = new ArrayList<>();
            // Each colour has equal weight, including all three Jotunheim variants.
            addVariants(entries, "aether_legacy:aercloud", 4, 1, 3);
            addVariants(entries, "gctcore:jotunheim_aercloud", 3, 1, 3);
            addPool(event, "gctcore_aercloud", entries);
        }
    }

    private static void addVariants(List<LootEntry> entries, String id, int variants, int min, int max) {
        ResourceLocation itemId = new ResourceLocation(id);
        if (!Item.REGISTRY.containsKey(itemId)) { return; }
        Item item = Item.REGISTRY.getObject(itemId);
        if (item == null || item == Items.AIR) { return; }
        for (int meta = 0; meta < variants; meta++) {
            LootFunction[] functions = {
                    new SetCount(NO_CONDITIONS, new RandomValueRange(min, max)),
                    new SetMetadata(NO_CONDITIONS, new RandomValueRange(meta)),
                    new LootingEnchantBonus(NO_CONDITIONS, new RandomValueRange(0, 1), 0)
            };
            entries.add(new LootEntryItem(item, 1, 0, functions, NO_CONDITIONS, id + "/" + meta));
        }
    }

    private static void addPool(LootTableLoadEvent event, String name, List<LootEntry> entries) {
        if (!entries.isEmpty() && event.getTable().getPool(name) == null) {
            event.getTable().addPool(new LootPool(entries.toArray(new LootEntry[0]), NO_CONDITIONS,
                    new RandomValueRange(1), new RandomValueRange(0), name));
        }
    }
}
