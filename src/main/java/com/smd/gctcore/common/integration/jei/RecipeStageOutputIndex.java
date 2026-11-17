package com.smd.gctcore.common.integration.jei;

import crafttweaker.api.item.IItemStack;
import crafttweaker.mc1120.item.MCItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 每次阶段应用按物品缩小候选，最终仍由原来的 IItemStack.matches 判断。 */
public final class RecipeStageOutputIndex {
    private final Map<String, List<IItemStack>> outputs;
    private final Map<Item, Map<String, List<IItemStack>>> outputsByItem;

    public RecipeStageOutputIndex(Map<String, List<IItemStack>> outputs) {
        this.outputs = outputs;
        Map<Item, Map<String, List<IItemStack>>> index = new IdentityHashMap<>();
        for (Map.Entry<String, List<IItemStack>> entry : outputs.entrySet()) {
            for (IItemStack output : entry.getValue()) {
                // 自定义匹配器可能跨物品匹配，沿用原始完整候选。
                if (output.getClass() != MCItemStack.class) {
                    outputsByItem = null;
                    return;
                }
                Item item = ((ItemStack) output.getInternal()).getItem();
                index.computeIfAbsent(item, ignored -> new LinkedHashMap<>())
                        .computeIfAbsent(entry.getKey(), ignored -> new ArrayList<>()).add(output);
            }
        }
        outputsByItem = index;
    }

    public Set<Map.Entry<String, List<IItemStack>>> getCandidates(IItemStack stack) {
        if (outputsByItem == null) {
            return outputs.entrySet();
        }
        Map<String, List<IItemStack>> candidates = outputsByItem.get(((ItemStack) stack.getInternal()).getItem());
        return candidates == null ? Collections.emptySet() : candidates.entrySet();
    }
}
