package com.smd.gctcore.misc;

import com.smd.gctcore.common.items.*;
import com.smd.gctcore.common.items.bloodmagic.soulgem.SoulGem;
import com.smd.gctcore.common.items.botania.ItemAlfSpark;
import com.smd.gctcore.common.items.botania.ItemGaiaSpark;
import com.smd.gctcore.common.items.draconicevolution.ChaoticFluxCapacitor;
import com.smd.gctcore.common.items.draconicevolution.FrostburnFluxCapacitor;
import com.smd.gctcore.common.items.draconicevolution.OrderedFluxCapacitor;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ItemRegistry {

    public static ChaoticFluxCapacitor CHAOTIC_FLUX_CAPACITOR;
    public static OrderedFluxCapacitor ORDERED_FLUX_CAPACITOR;
    public static FrostburnFluxCapacitor FROSTBURN_FLUX_CAPACITOR;
    public static Item ITEM_SOUL_GEM;
    public static Item CRIMSON_ANCHOR;
    public static Item ALF_SPARK;
    public static Item GAIA_SPARK;
    public static Item RAW_QUARTZ;
    public static Item SHAPED_QUARTZ;
    public static Item MMCE_BUILDER_TOOL;

    public static void init() {
        CHAOTIC_FLUX_CAPACITOR = new ChaoticFluxCapacitor();
        ORDERED_FLUX_CAPACITOR = new OrderedFluxCapacitor();
        FROSTBURN_FLUX_CAPACITOR = new FrostburnFluxCapacitor();
        ITEM_SOUL_GEM = new SoulGem();
        CRIMSON_ANCHOR = new CrimsonAnchorItem();
        ALF_SPARK = new ItemAlfSpark();
        GAIA_SPARK = new ItemGaiaSpark();
        RAW_QUARTZ = new RawQuartzItem();
        SHAPED_QUARTZ = new ShapedQuartzItem();
        MMCE_BUILDER_TOOL = new MMCE_BuilderTool();
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
                RAW_QUARTZ,
                SHAPED_QUARTZ,
                MMCE_BUILDER_TOOL
        );
    }
}
