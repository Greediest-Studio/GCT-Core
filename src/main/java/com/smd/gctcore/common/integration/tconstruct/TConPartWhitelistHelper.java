package com.smd.gctcore.common.integration.tconstruct;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Matches Tinkers' Construct part items against the whitelist configured in tconstruct.cfg
 * so selected parts can still list all valid material variants when listAllPartMaterials is false.
 */
public final class TConPartWhitelistHelper {

    private static volatile boolean enabled = true;
    private static volatile WhitelistSnapshot cachedSnapshot = WhitelistSnapshot.EMPTY;

    private TConPartWhitelistHelper() {
    }

    public static void updateConfig(boolean enableWhitelist, @Nullable String[] entries) {
        enabled = enableWhitelist;
        cachedSnapshot = compile(entries);
    }

    public static boolean shouldListAllMaterials(@Nullable Item item) {
        if (item == null || !enabled) {
            return false;
        }
        ResourceLocation registryName = item.getRegistryName();
        if (registryName == null) {
            return false;
        }
        return cachedSnapshot.matches(registryName);
    }

    static WhitelistSnapshot compile(@Nullable String[] entries) {
        if (entries == null || entries.length == 0) {
            return WhitelistSnapshot.EMPTY;
        }

        Set<String> exactItems = new HashSet<>();
        Set<String> wildcardNamespaces = new HashSet<>();

        for (String raw : entries) {
            if (raw == null) {
                continue;
            }
            String entry = raw.trim().toLowerCase(Locale.ROOT);
            if (entry.isEmpty()) {
                continue;
            }

            if (entry.endsWith(":*")) {
                String namespace = entry.substring(0, entry.length() - 2).trim();
                if (!namespace.isEmpty()) {
                    wildcardNamespaces.add(namespace);
                }
                continue;
            }

            if (!entry.contains(":")) {
                entry = "tconstruct:" + entry;
            }
            exactItems.add(entry);
        }

        if (exactItems.isEmpty() && wildcardNamespaces.isEmpty()) {
            return WhitelistSnapshot.EMPTY;
        }
        return new WhitelistSnapshot(
                Collections.unmodifiableSet(exactItems),
                Collections.unmodifiableSet(wildcardNamespaces)
        );
    }

    static final class WhitelistSnapshot {
        static final WhitelistSnapshot EMPTY =
                new WhitelistSnapshot(Collections.emptySet(), Collections.emptySet());

        private final Set<String> exactItems;
        private final Set<String> wildcardNamespaces;

        WhitelistSnapshot(Set<String> exactItems, Set<String> wildcardNamespaces) {
            this.exactItems = exactItems;
            this.wildcardNamespaces = wildcardNamespaces;
        }

        boolean matches(ResourceLocation registryName) {
            return wildcardNamespaces.contains(registryName.getNamespace().toLowerCase(Locale.ROOT))
                    || exactItems.contains(registryName.toString().toLowerCase(Locale.ROOT));
        }
    }
}
