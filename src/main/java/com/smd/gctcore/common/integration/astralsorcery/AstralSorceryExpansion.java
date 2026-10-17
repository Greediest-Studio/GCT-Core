package com.smd.gctcore.common.integration.astralsorcery;

import com.smd.gctcore.Tags;
import net.minecraft.block.Block;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import hellfirepvp.astralsorcery.common.crafting.altar.AltarRecipeRegistry;

/** Registration bridge for GCT-Core's optional Astral Sorcery content. */
public final class AstralSorceryExpansion {

    public static EternalCrystalItem ETERNAL_CRYSTAL;
    public static EternalTunedCrystalItem TUNED_ETERNAL_CRYSTAL;
    public static EternalCollectorCrystalBlock ETERNAL_COLLECTOR_CRYSTAL;
    public static EternalCollectorCrystalItem ETERNAL_COLLECTOR_CRYSTAL_ITEM;
    public static EnumRarity ETERNAL_RARITY;

    private static boolean initialized;
    private static boolean recipeRegistered;

    private AstralSorceryExpansion() {}

    public static void init() {
        if (initialized) return;
        initialized = true;

        ETERNAL_RARITY = EnumHelper.addRarity("ETERNAL", TextFormatting.AQUA, "Eternal");

        ETERNAL_CRYSTAL = (EternalCrystalItem) new EternalCrystalItem()
                .setRegistryName(Tags.MOD_ID, "eternal_crystal")
                .setTranslationKey("gctcore.eternal_crystal");
        TUNED_ETERNAL_CRYSTAL = (EternalTunedCrystalItem) new EternalTunedCrystalItem()
                .setRegistryName(Tags.MOD_ID, "tuned_eternal_crystal")
                .setTranslationKey("gctcore.tuned_eternal_crystal");
        ETERNAL_COLLECTOR_CRYSTAL = (EternalCollectorCrystalBlock) new EternalCollectorCrystalBlock()
                .setRegistryName(Tags.MOD_ID, "eternal_collector_crystal")
                .setTranslationKey("gctcore.eternal_collector_crystal");
        ETERNAL_COLLECTOR_CRYSTAL_ITEM = (EternalCollectorCrystalItem) new EternalCollectorCrystalItem(ETERNAL_COLLECTOR_CRYSTAL)
                .setRegistryName(Tags.MOD_ID, "eternal_collector_crystal")
                .setTranslationKey("gctcore.eternal_collector_crystal");

        MinecraftForge.EVENT_BUS.register(new AstralSorceryExpansion());
        GameRegistry.registerTileEntity(EternalCollectorCrystalTile.class,
                new ResourceLocation(Tags.MOD_ID, "eternal_collector_crystal"));
    }

    public static void registerRecipes() {
        if (!initialized || recipeRegistered) return;
        recipeRegistered = true;
        AltarRecipeRegistry.registerAltarRecipe(new EternalCollectorCrystalRecipe());
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(ETERNAL_COLLECTOR_CRYSTAL);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                ETERNAL_CRYSTAL,
                TUNED_ETERNAL_CRYSTAL,
                ETERNAL_COLLECTOR_CRYSTAL_ITEM);
    }
}
