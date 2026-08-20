package com.smd.gctcore.client;

import com.smd.gctcore.Tags;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashMap;
import java.util.Map;

/** Adds the tier statistics to Botania and Botaniverse spark items. */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class SparkTooltipHandler {

    private static final String TOOLTIP_PREFIX = "greedycraft.tooltip.botania.spark.";
    private static final Map<ResourceLocation, String> TOOLTIP_TIERS = new HashMap<>();

    static {
        TOOLTIP_TIERS.put(new ResourceLocation("botania", "spark"), "basic");
        TOOLTIP_TIERS.put(new ResourceLocation("botaniverse", "spark_nilfheim"), "niflheim");
        TOOLTIP_TIERS.put(new ResourceLocation("botaniverse", "spark_muspelheim"), "muspelheim");
        // GreedyCraft presents Botaniverse's Alfheim tier as Helheim.
        TOOLTIP_TIERS.put(new ResourceLocation("botaniverse", "spark_alfheim"), "helheim");
        TOOLTIP_TIERS.put(new ResourceLocation("botaniverse", "spark_asgard"), "asgard");
    }

    private SparkTooltipHandler() {
    }

    @SubscribeEvent
    public static void addSparkStatistics(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || stack.getItem().getRegistryName() == null) {
            return;
        }

        String tier = TOOLTIP_TIERS.get(stack.getItem().getRegistryName());
        if (tier == null) {
            return;
        }

        String key = TOOLTIP_PREFIX + tier;
        String localized = I18n.translateToLocal(key);
        if (localized == null || localized.equals(key)) {
            return;
        }

        for (String line : localized.replace("\\n", "\n").split("\\r?\\n", -1)) {
            event.getToolTip().add(line);
        }
    }
}
