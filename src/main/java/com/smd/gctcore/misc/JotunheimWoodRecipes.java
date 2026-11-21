package com.smd.gctcore.misc;

import com.smd.gctcore.Tags;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

/** Standard wooden building recipes, plus material-conserving dismantling recipes. */
public final class JotunheimWoodRecipes {
    private JotunheimWoodRecipes() { }

    public static void init() {
        for (JotunheimWoodBlocks.Family family : JotunheimWoodBlocks.families()) {
            String prefix = "jotunheim_" + family.key;
            shapeless(prefix + "_planks", new ItemStack(family.planks, 4), wildcard(family.log()));
            if ("jotunn_oak".equals(family.key) || "elder_pine".equals(family.key) || "sunkenwood".equals(family.key)) {
                shapeless(prefix + "_planks_from_bark", new ItemStack(family.planks, 4),
                        wildcard(JotunheimBlocks.get(family.key + "_bark")));
            }
            shaped(prefix + "_slab", new ItemStack(family.slab, 6), "PPP", 'P', family.planks);
            shaped(prefix + "_stairs", new ItemStack(family.stairs, 4), "P  ", "PP ", "PPP", 'P', family.planks);
            shaped(prefix + "_fence", new ItemStack(family.fence, 3), "PSP", "PSP", 'P', family.planks, 'S', Items.STICK);
            shaped(prefix + "_fence_gate", new ItemStack(family.fenceGate), "SPS", "SPS", 'P', family.planks, 'S', Items.STICK);

            shapeless(prefix + "_planks_from_slabs", new ItemStack(family.planks), wildcard(family.slab), wildcard(family.slab));
            shapeless(prefix + "_planks_from_stairs", new ItemStack(family.planks, 3), wildcard(family.stairs), wildcard(family.stairs));
            // Dismantling returns only the original planks; the connecting sticks are consumed.
            shapeless(prefix + "_planks_from_fences", new ItemStack(family.planks, 4),
                    wildcard(family.fence), wildcard(family.fence), wildcard(family.fence));
            shapeless(prefix + "_planks_from_fence_gate", new ItemStack(family.planks, 2), wildcard(family.fenceGate));
        }
    }

    private static Ingredient wildcard(Block block) {
        return Ingredient.fromStacks(new ItemStack(block, 1, OreDictionary.WILDCARD_VALUE));
    }

    private static void shaped(String name, ItemStack output, Object... input) {
        GameRegistry.addShapedRecipe(new ResourceLocation(Tags.MOD_ID, name), null, output, input);
    }

    private static void shapeless(String name, ItemStack output, Ingredient... input) {
        GameRegistry.addShapelessRecipe(new ResourceLocation(Tags.MOD_ID, name), null, output, input);
    }
}
