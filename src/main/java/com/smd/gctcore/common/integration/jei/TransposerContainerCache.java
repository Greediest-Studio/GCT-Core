package com.smd.gctcore.common.integration.jei;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** 注液和抽液的探测步骤相同，单次 JEI 加载内合并探测，分别保留展示结果。 */
public final class TransposerContainerCache {
    private static final Map<ItemStack, Pair> CACHE = new IdentityHashMap<>();
    private static boolean active;

    private TransposerContainerCache() {
    }

    public static void beginLoad() {
        CACHE.clear();
        active = true;
    }

    public static void endLoad() {
        active = false;
        CACHE.clear();
    }

    public static Pair get(ItemStack container) {
        // 使用 JEI 原物品对象的身份，避免合并 NBT 或能力状态不同的容器。
        Pair cached = active ? CACHE.get(container) : null;
        if (cached != null) return cached;
        IFluidHandlerItem handler = container.copy().getCapability(
                CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        Pair result = probe(container, handler, FluidRegistry.getRegisteredFluids().values());
        if (active) CACHE.put(container, result);
        return result;
    }

    static Pair probe(ItemStack container, IFluidHandlerItem handler, Iterable<Fluid> fluids) {
        Pair result = new Pair();
        // 保留原循环的同一个 handler、1000mB 探测量及连续填充/抽取顺序。
        for (Fluid fluid : fluids) {
            int filled = handler.fill(new FluidStack(fluid, 1000), true);
            if (filled <= 0) continue;
            FluidStack fillFluid = new FluidStack(fluid, filled);
            ItemStack filledContainer = handler.getContainer();
            result.fill.inputs.add(container);
            result.fill.outputs.add(filledContainer.copy());
            result.fill.fluids.add(fillFluid);
            ItemStack extractInput = filledContainer.copy();
            FluidStack drained = handler.drain(1000, true);
            if (drained != null) {
                result.extract.inputs.add(extractInput);
                result.extract.outputs.add(handler.getContainer());
                result.extract.fluids.add(drained);
            }
        }
        return result;
    }

    public static final class Pair {
        public final Recipes fill = new Recipes();
        public final Recipes extract = new Recipes();
    }

    public static final class Recipes {
        public final List<ItemStack> inputs = new ArrayList<>();
        public final List<ItemStack> outputs = new ArrayList<>();
        public final List<FluidStack> fluids = new ArrayList<>();
    }
}
