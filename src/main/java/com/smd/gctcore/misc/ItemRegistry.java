package com.smd.gctcore.misc;

import com.smd.gctcore.common.items.*;
import com.smd.gctcore.common.items.bloodmagic.soulgem.SoulGem;
import com.smd.gctcore.common.items.botania.ItemAlfSpark;
import com.smd.gctcore.common.items.botania.ItemGaiaSpark;
import com.smd.gctcore.common.items.botania.ItemJoetunheimSpark;
import com.smd.gctcore.common.items.botania.ItemNidavellirSpark;
import com.smd.gctcore.common.items.botania.ItemVanaheimSpark;
import com.smd.gctcore.common.items.draconicevolution.ChaoticFluxCapacitor;
import com.smd.gctcore.common.items.draconicevolution.FrostburnFluxCapacitor;
import com.smd.gctcore.common.items.draconicevolution.OrderedFluxCapacitor;
import com.smd.gctcore.common.items.mekanism.ItemEnergyMk2Upgrade;
import com.smd.gctcore.common.items.mekanism.ItemMiningLevelUpgrade;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ItemRegistry {

    public static ChaoticFluxCapacitor CHAOTIC_FLUX_CAPACITOR;
    public static OrderedFluxCapacitor ORDERED_FLUX_CAPACITOR;
    public static FrostburnFluxCapacitor FROSTBURN_FLUX_CAPACITOR;
    public static Item ITEM_SOUL_GEM;
    public static Item CRIMSON_ANCHOR;
    public static Item ALF_SPARK;
    public static Item GAIA_SPARK;
    public static Item JOETUNHEIM_SPARK;
    public static Item NIDAVELLIR_SPARK;
    public static Item VANAHEIM_SPARK;
    public static Item RAW_QUARTZ;
    public static Item SHAPED_QUARTZ;
    public static Item APATHY_INGOT;
    public static Item IMAGINATIVE_SNOWBALL;
    public static Item MMCE_BUILDER_TOOL;
    public static Item BIRD_OF_EDWIN;
    public static Item MINING_LEVEL_UPGRADE;
    public static Item ENERGY_MK2_UPGRADE;

    public static void init() {
        CHAOTIC_FLUX_CAPACITOR = new ChaoticFluxCapacitor();
        ORDERED_FLUX_CAPACITOR = new OrderedFluxCapacitor();
        FROSTBURN_FLUX_CAPACITOR = new FrostburnFluxCapacitor();
        ITEM_SOUL_GEM = new SoulGem();
        CRIMSON_ANCHOR = new CrimsonAnchorItem();
        ALF_SPARK = new ItemAlfSpark();
        GAIA_SPARK = new ItemGaiaSpark();
        JOETUNHEIM_SPARK = new ItemJoetunheimSpark();
        NIDAVELLIR_SPARK = new ItemNidavellirSpark();
        VANAHEIM_SPARK = new ItemVanaheimSpark();
        RAW_QUARTZ = new RawQuartzItem();
        SHAPED_QUARTZ = new ShapedQuartzItem();
        APATHY_INGOT = new Item()
                .setRegistryName("apathy_ingot")
                .setTranslationKey("gctcore.apathy_ingot")
                .setCreativeTab(CreativeTabs.MATERIALS);
        IMAGINATIVE_SNOWBALL = new ImaginativeSnowballItem();
        MMCE_BUILDER_TOOL = new MMCE_BuilderTool();
        BIRD_OF_EDWIN = new BirdOfEdwin();
        if (Loader.isModLoaded("mekanism")) {
            MINING_LEVEL_UPGRADE = new ItemMiningLevelUpgrade();
            ENERGY_MK2_UPGRADE = new ItemEnergyMk2Upgrade();
        }
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                CHAOTIC_FLUX_CAPACITOR,
                ORDERED_FLUX_CAPACITOR,
                FROSTBURN_FLUX_CAPACITOR,
                ITEM_SOUL_GEM,
                CRIMSON_ANCHOR,
                ALF_SPARK,
                GAIA_SPARK,
                JOETUNHEIM_SPARK,
                NIDAVELLIR_SPARK,
                VANAHEIM_SPARK,
                RAW_QUARTZ,
                SHAPED_QUARTZ,
                APATHY_INGOT,
                IMAGINATIVE_SNOWBALL,
                MMCE_BUILDER_TOOL,
                BIRD_OF_EDWIN
        );
        if (MINING_LEVEL_UPGRADE != null) {
            event.getRegistry().register(MINING_LEVEL_UPGRADE);
        }
        if (ENERGY_MK2_UPGRADE != null) {
            event.getRegistry().register(ENERGY_MK2_UPGRADE);
        }
    }
}
