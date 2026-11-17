package com.smd.gctcore.common.integration.aether;

import com.smd.gctcore.gctcore;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class AetherZephyrNameFix {

    private static final ResourceLocation ZEPHYR_ID = new ResourceLocation("aether_legacy", "zephyr");
    private static final String ZEPHYR_CLASS = "com.gildedgames.the_aether.entities.hostile.EntityZephyr";

    private AetherZephyrNameFix() {
    }

    public static void apply() {
        if (!Loader.isModLoaded("aether_legacy")) return;

        EntityEntry entry = ForgeRegistries.ENTITIES.getValue(ZEPHYR_ID);
        if (entry == null || !ZEPHYR_CLASS.equals(entry.getEntityClass().getName())) {
            gctcore.LOGGER.warn("Could not find Aether's Zephyr entity entry; its display name was not changed.");
            return;
        }

        try {
            ObfuscationReflectionHelper.setPrivateValue(
                    EntityEntry.class, entry, "gctcore.aether_zephyr", "name");
        } catch (RuntimeException exception) {
            gctcore.LOGGER.error("Could not change Aether's Zephyr translation key; continuing with its original name.", exception);
        }
    }
}
