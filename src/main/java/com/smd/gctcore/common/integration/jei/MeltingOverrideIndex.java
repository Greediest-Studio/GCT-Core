package com.smd.gctcore.common.integration.jei;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import slimeknights.mantle.util.RecipeMatch;
import slimeknights.tconstruct.library.smeltery.MeltingRecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

/** 单次 JEI 配方生成使用的候选索引；对常见 Item/Oredict 匹配跳过 NonNullList 与 LinkedList 分配，其余保留全量匹配。 */
public final class MeltingOverrideIndex {
    private static final int WILDCARD_METADATA = 32767;

    private final Map<Item, OverrideBucket> byItem = new IdentityHashMap<>();
    private final List<MeltingRecipe> fallback = new ArrayList<>();

    public MeltingOverrideIndex(List<MeltingRecipe> overrides) {
        for (MeltingRecipe recipe : overrides) {
            if (recipe.getClass() != MeltingRecipe.class || recipe.input == null) {
                fallback.add(recipe);
                continue;
            }
            Class<?> matchClass = recipe.input.getClass();
            if (matchClass == RecipeMatch.Item.class || matchClass == RecipeMatch.Oredict.class) {
                indexOreOrItemMatch(recipe);
            } else if (matchClass == RecipeMatch.ItemCombination.class) {
                indexCombinationMatch(recipe);
            } else {
                fallback.add(recipe);
            }
        }
    }

    private void indexOreOrItemMatch(MeltingRecipe recipe) {
        int amountNeeded = recipe.input.amountNeeded;
        // amountNeeded <= 0 时 Oredict.getInputs() 为空但仍匹配，必须保留全量匹配。
        if (amountNeeded <= 0) {
            fallback.add(recipe);
            return;
        }
        List<ItemStack> inputs = recipe.input.getInputs();
        if (inputs.isEmpty()) {
            // amountNeeded > 0 且 getInputs() 为空，仅会在空 Oredict 时发生，matches() 恒为 false。
            return;
        }
        ItemStack prev = null;
        for (ItemStack target : inputs) {
            if (target == prev) {
                continue;
            }
            prev = target;
            if (target.isEmpty()) {
                fallback.add(recipe);
                return;
            }
            OverrideBucket bucket = byItem.computeIfAbsent(target.getItem(), item -> new OverrideBucket());
            bucket.addAmountTarget(target.getMetadata(), amountNeeded);
        }
    }

    private void indexCombinationMatch(MeltingRecipe recipe) {
        List<ItemStack> inputs = recipe.input.getInputs();
        if (inputs.isEmpty() || inputs.stream().allMatch(ItemStack::isEmpty)) {
            fallback.add(recipe);
            return;
        }
        Set<Item> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (ItemStack input : inputs) {
            if (!input.isEmpty() && seen.add(input.getItem())) {
                byItem.computeIfAbsent(input.getItem(), item -> new OverrideBucket()).addComplex(recipe);
            }
        }
    }

    public boolean matchesAny(List<ItemStack> inputs) {
        ItemStack prev = null;
        for (ItemStack input : inputs) {
            if (input == prev) {
                continue;
            }
            prev = input;
            if (!input.isEmpty()) {
                OverrideBucket bucket = byItem.get(input.getItem());
                if (bucket != null && bucket.matches(input)) {
                    return true;
                }
            }
            if (!fallback.isEmpty() && matches(fallback, input)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matches(List<MeltingRecipe> recipes, ItemStack input) {
        for (MeltingRecipe recipe : recipes) {
            if (recipe.matches(input)) return true;
        }
        return false;
    }

    private static final class OverrideBucket {
        int minWildcardAmount = Integer.MAX_VALUE;
        Int2IntOpenHashMap minByMeta;
        List<MeltingRecipe> complex;

        void addAmountTarget(int metadata, int amountNeeded) {
            if (metadata == WILDCARD_METADATA) {
                if (amountNeeded < this.minWildcardAmount) {
                    this.minWildcardAmount = amountNeeded;
                }
                return;
            }
            if (this.minByMeta == null) {
                this.minByMeta = new Int2IntOpenHashMap();
                this.minByMeta.defaultReturnValue(Integer.MAX_VALUE);
            }
            int prev = this.minByMeta.get(metadata);
            if (amountNeeded < prev) {
                this.minByMeta.put(metadata, amountNeeded);
            }
        }

        void addComplex(MeltingRecipe recipe) {
            if (this.complex == null) {
                this.complex = new ArrayList<>(2);
            }
            this.complex.add(recipe);
        }

        boolean matches(ItemStack input) {
            int count = input.getCount();
            if (count >= this.minWildcardAmount) {
                return true;
            }
            if (this.minByMeta != null && count >= this.minByMeta.get(input.getMetadata())) {
                return true;
            }
            return this.complex != null && MeltingOverrideIndex.matches(this.complex, input);
        }
    }
}
