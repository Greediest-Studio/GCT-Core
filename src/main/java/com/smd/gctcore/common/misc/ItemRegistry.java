package com.smd.gctcore.common.misc;

import com.smd.gctcore.common.items.CrimsonAnchorItem;
import com.smd.gctcore.common.items.bloodmagic.soulgem.SoulGem;
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

    public static void init() {
        CHAOTIC_FLUX_CAPACITOR = new ChaoticFluxCapacitor();
        ORDERED_FLUX_CAPACITOR = new OrderedFluxCapacitor();
        FROSTBURN_FLUX_CAPACITOR = new FrostburnFluxCapacitor();
        ITEM_SOUL_GEM = new SoulGem();
        CRIMSON_ANCHOR = new CrimsonAnchorItem();
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                CHAOTIC_FLUX_CAPACITOR,
                ORDERED_FLUX_CAPACITOR,
                FROSTBURN_FLUX_CAPACITOR,
                ITEM_SOUL_GEM,
                CRIMSON_ANCHOR
        );
    }
}
