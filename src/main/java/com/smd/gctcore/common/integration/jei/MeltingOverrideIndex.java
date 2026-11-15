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

/** 单次 JEI 配方生成使用的候选索引；最终匹配仍交给配方本身。 */
public final class MeltingOverrideIndex {
    private final Map<Item, List<MeltingRecipe>> byItem = new IdentityHashMap<>();
    private final List<MeltingRecipe> fallback = new ArrayList<>();

    public MeltingOverrideIndex(List<MeltingRecipe> overrides) {
        for (MeltingRecipe recipe : overrides) {
            // 自定义配方/匹配器可能接受 getInputs() 未列出的物品，保留全量匹配。
            if (recipe.getClass() != MeltingRecipe.class || !hasEnumerableInputs(recipe.input)) {
                fallback.add(recipe);
                continue;
            }
            List<ItemStack> inputs = recipe.input.getInputs();
            if (inputs.isEmpty() || inputs.stream().allMatch(ItemStack::isEmpty)) {
                fallback.add(recipe);
                continue;
            }
            Set<Item> seen = Collections.newSetFromMap(new IdentityHashMap<>());
            for (ItemStack input : inputs) {
                if (seen.add(input.getItem())) {
                    byItem.computeIfAbsent(input.getItem(), item -> new ArrayList<>()).add(recipe);
                }
            }
        }
    }

    private static boolean hasEnumerableInputs(RecipeMatch input) {
        return input != null && (input.getClass() == RecipeMatch.Item.class
                || input.getClass() == RecipeMatch.Oredict.class
                || input.getClass() == RecipeMatch.ItemCombination.class);
    }

    public boolean matchesAny(List<ItemStack> inputs) {
        for (ItemStack input : inputs) {
            List<MeltingRecipe> candidates = byItem.get(input.getItem());
            if (candidates != null && matches(candidates, input)) return true;
            if (matches(fallback, input)) return true;
        }
        return false;
    }

    private static boolean matches(List<MeltingRecipe> recipes, ItemStack input) {
        for (MeltingRecipe recipe : recipes) {
            if (recipe.matches(input)) return true;
        }
        return false;
    }
}
