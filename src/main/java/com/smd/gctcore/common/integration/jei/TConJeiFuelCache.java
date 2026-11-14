package com.smd.gctcore.common.integration.jei;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.tconstruct.library.TinkerRegistry;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Shares immutable JEI smeltery fuel lists across melting recipe wrappers that
 * have the same eligible fuel subset instead of cloning all fuel FluidStacks per recipe.
 */
public final class TConJeiFuelCache {

    private static final Object LOCK = new Object();
    private static FuelEntry[] cachedEntries = new FuelEntry[0];
    private static final Map<Integer, List<FluidStack>> TEMP_TO_LIST = new HashMap<>();
    private static final Map<BitSet, ImmutableList<FluidStack>> SUBSET_TO_LIST = new HashMap<>();

    private TConJeiFuelCache() {
    }

    public static List<FluidStack> getSharedFuels(int minTemperature) {
        Collection<FluidStack> sourceFuels = TinkerRegistry.getSmelteryFuels();
        if (sourceFuels == null || sourceFuels.isEmpty()) {
            return Collections.emptyList();
        }

        synchronized (LOCK) {
            if (!matchesCachedSources(sourceFuels)) {
                rebuildCache(sourceFuels);
            }
            if (cachedEntries.length == 0) {
                return Collections.emptyList();
            }

            List<FluidStack> cached = TEMP_TO_LIST.get(minTemperature);
            if (cached != null) {
                return cached;
            }

            BitSet mask = new BitSet(cachedEntries.length);
            for (int i = 0; i < cachedEntries.length; i++) {
                if (cachedEntries[i].temperature >= minTemperature) {
                    mask.set(i);
                }
            }

            ImmutableList<FluidStack> shared = SUBSET_TO_LIST.get(mask);
            if (shared == null) {
                if (mask.isEmpty()) {
                    shared = ImmutableList.of();
                } else {
                    ImmutableList.Builder<FluidStack> builder = ImmutableList.builder();
                    for (int i = mask.nextSetBit(0); i >= 0; i = mask.nextSetBit(i + 1)) {
                        builder.add(cachedEntries[i].normalizedStack);
                    }
                    shared = builder.build();
                }
                SUBSET_TO_LIST.put((BitSet) mask.clone(), shared);
            }

            TEMP_TO_LIST.put(minTemperature, shared);
            return shared;
        }
    }

    private static boolean matchesCachedSources(Collection<FluidStack> sourceFuels) {
        int i = 0;
        for (FluidStack fuel : sourceFuels) {
            if (fuel == null || fuel.getFluid() == null) {
                continue;
            }
            if (i >= cachedEntries.length) {
                return false;
            }
            FuelEntry entry = cachedEntries[i];
            if (entry.sourceRef != fuel
                    || entry.sourceFluid != fuel.getFluid()
                    || entry.temperature != fuel.getFluid().getTemperature(fuel)
                    || !Objects.equals(entry.normalizedStack.tag, fuel.tag)) {
                return false;
            }
            i++;
        }
        return i == cachedEntries.length;
    }

    private static void rebuildCache(Collection<FluidStack> sourceFuels) {
        TEMP_TO_LIST.clear();
        SUBSET_TO_LIST.clear();

        List<FuelEntry> entries = new ArrayList<>(sourceFuels.size());
        for (FluidStack fuel : sourceFuels) {
            if (fuel == null || fuel.getFluid() == null) {
                continue;
            }
            Fluid fluid = fuel.getFluid();
            int temp = fluid.getTemperature(fuel);
            FluidStack normalized = fuel.copy();
            normalized.amount = 1000;
            entries.add(new FuelEntry(fuel, fluid, temp, normalized));
        }
        cachedEntries = entries.toArray(new FuelEntry[0]);
    }

    private static final class FuelEntry {
        final FluidStack sourceRef;
        final Fluid sourceFluid;
        final int temperature;
        final FluidStack normalizedStack;

        FuelEntry(FluidStack sourceRef, Fluid sourceFluid, int temperature, FluidStack normalizedStack) {
            this.sourceRef = sourceRef;
            this.sourceFluid = sourceFluid;
            this.temperature = temperature;
            this.normalizedStack = normalizedStack;
        }
    }
}
