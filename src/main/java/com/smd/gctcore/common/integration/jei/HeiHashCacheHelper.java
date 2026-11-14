package com.smd.gctcore.common.integration.jei;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Keeps HEI's ItemStackHelper and FluidStackHelper hash caches active only during
 * JEI startup/reload, then clears and shrinks them so stacks and NBT compounds
 * are not pinned permanently in heap.
 */
public final class HeiHashCacheHelper {

    private static final Object LOCK = new Object();
    private static final List<WeakReference<Object2IntMap<ItemStack>>> ITEM_CACHES = new ArrayList<>();
    private static final List<WeakReference<Map<FluidStack, Integer>>> FLUID_CACHES = new ArrayList<>();
    private static volatile boolean cachingActive = false;

    private HeiHashCacheHelper() {
    }

    public static boolean isCachingActive() {
        return cachingActive;
    }

    public static void beginLoad() {
        cachingActive = true;
    }

    public static void endLoad() {
        cachingActive = false;
        clearAndTrimAll();
    }

    public static void registerItemHashCache(Object2IntMap<ItemStack> cache) {
        if (cache == null) {
            return;
        }
        synchronized (LOCK) {
            addWeakUnique(ITEM_CACHES, cache);
        }
    }

    public static void registerFluidHashCache(Map<FluidStack, Integer> cache) {
        if (cache == null) {
            return;
        }
        synchronized (LOCK) {
            addWeakUnique(FLUID_CACHES, cache);
        }
    }

    private static <T> void addWeakUnique(List<WeakReference<T>> refs, T target) {
        for (Iterator<WeakReference<T>> it = refs.iterator(); it.hasNext(); ) {
            T existing = it.next().get();
            if (existing == null) {
                it.remove();
            } else if (existing == target) {
                return;
            }
        }
        refs.add(new WeakReference<>(target));
    }

    private static void clearAndTrimAll() {
        synchronized (LOCK) {
            for (Iterator<WeakReference<Object2IntMap<ItemStack>>> it = ITEM_CACHES.iterator(); it.hasNext(); ) {
                Object2IntMap<ItemStack> map = it.next().get();
                if (map == null) {
                    it.remove();
                    continue;
                }
                map.clear();
                if (map instanceof Object2IntOpenHashMap) {
                    ((Object2IntOpenHashMap<?>) map).trim();
                }
            }
            for (Iterator<WeakReference<Map<FluidStack, Integer>>> it = FLUID_CACHES.iterator(); it.hasNext(); ) {
                Map<FluidStack, Integer> map = it.next().get();
                if (map == null) {
                    it.remove();
                    continue;
                }
                map.clear();
                if (map instanceof Object2IntOpenHashMap) {
                    ((Object2IntOpenHashMap<?>) map).trim();
                }
            }
        }
    }
}
