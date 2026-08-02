package com.smd.gctcore.common.integration.extendedcrafting;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/** Keeps the integration compatible with both upstream and Nomifactory ExtendedCrafting builds. */
public final class ExtendedCraftingRecipeBridge {
    private static final String MANAGER = "com.blakebr0.extendedcrafting.crafting.table.TableRecipeManager";
    private static Method getInstance;
    private static Method getRecipesTiered;
    private static boolean initialized;

    private ExtendedCraftingRecipeBridge() {
    }

    public static Match findMatch(ExtendedCraftingTier tier, List<ItemStack> stacks, World world) {
        InventoryCrafting grid = makeGrid(tier, stacks);
        for (Object candidate : recipes(tier.level())) {
            if (!(candidate instanceof IRecipe)) {
                continue;
            }
            IRecipe recipe = (IRecipe) candidate;
            try {
                if (recipe.matches(grid, world)) {
                    ItemStack output = recipe.getCraftingResult(grid);
                    if (!output.isEmpty()) {
                        return new Match(recipe, grid, output, recipe.getRemainingItems(grid));
                    }
                }
            } catch (RuntimeException ignored) {
                // A broken CraftTweaker recipe must not make the entire AE network unusable.
            }
        }
        return null;
    }

    public static InventoryCrafting makeGrid(ExtendedCraftingTier tier, List<ItemStack> stacks) {
        InventoryCrafting grid = new InventoryCrafting(new DummyContainer(), tier.gridSize(), tier.gridSize());
        int limit = Math.min(grid.getSizeInventory(), stacks.size());
        for (int i = 0; i < limit; i++) {
            ItemStack stack = stacks.get(i);
            if (stack != null && !stack.isEmpty()) {
                ItemStack one = stack.copy();
                one.setCount(1);
                grid.setInventorySlotContents(i, one);
            }
        }
        return grid;
    }

    @SuppressWarnings("unchecked")
    private static List<?> recipes(int tier) {
        initialize();
        if (getInstance == null || getRecipesTiered == null) {
            return Collections.emptyList();
        }
        try {
            Object manager = getInstance.invoke(null);
            Object recipes = getRecipesTiered.invoke(manager, tier);
            return recipes instanceof List ? (List<?>) recipes : Collections.emptyList();
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return Collections.emptyList();
        }
    }

    private static synchronized void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        try {
            Class<?> manager = Class.forName(MANAGER);
            getInstance = manager.getMethod("getInstance");
            getRecipesTiered = manager.getMethod("getRecipesTiered", int.class);
        } catch (ReflectiveOperationException ignored) {
            getInstance = null;
            getRecipesTiered = null;
        }
    }

    private static final class DummyContainer extends Container {
        @Override
        public boolean canInteractWith(net.minecraft.entity.player.EntityPlayer playerIn) {
            return false;
        }
    }

    public static final class Match {
        private final IRecipe recipe;
        private final InventoryCrafting grid;
        private final ItemStack output;
        private final NonNullList<ItemStack> remaining;

        private Match(IRecipe recipe, InventoryCrafting grid, ItemStack output, NonNullList<ItemStack> remaining) {
            this.recipe = recipe;
            this.grid = grid;
            this.output = output.copy();
            this.remaining = remaining;
        }

        public IRecipe recipe() {
            return recipe;
        }

        public InventoryCrafting grid() {
            return grid;
        }

        public ItemStack output() {
            return output.copy();
        }

        public NonNullList<ItemStack> remaining() {
            return remaining;
        }
    }
}
