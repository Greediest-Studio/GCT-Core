package com.smd.gctcore.misc;

import com.smd.gctcore.Tags;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

/** Ordinary stone masonry recipes; no additional progression materials. */
public final class YmirRecipes {
    private YmirRecipes() { }

    public static void init() {
        for (YmirBlocks.Family family : YmirBlocks.families()) {
            shaped(family.prefix + "_bricks", new ItemStack(family.bricks, 4), "SS", "SS", 'S', family.stone);
            shaped(family.prefix + "_stairs", new ItemStack(family.stairs, 4), "B  ", "BB ", "BBB", 'B', family.bricks);
            shaped(family.prefix + "_slab", new ItemStack(family.slab, 6), "BBB", 'B', family.bricks);
            shaped(family.prefix + "_wall", new ItemStack(family.wall, 6), "BBB", "BBB", 'B', family.bricks);
            shaped(family.prefix + "_pillar", new ItemStack(family.pillar, 2), "B", "B", 'B', family.bricks);
            shaped("chiseled_" + family.prefix, new ItemStack(family.chiseled), "S", "S", 'S', family.slab);
            GameRegistry.addSmelting(family.bricks, new ItemStack(family.crackedBricks), 0.1F);
        }
    }

    private static void shaped(String key, ItemStack output, Object... input) {
        GameRegistry.addShapedRecipe(new ResourceLocation(Tags.MOD_ID, key), null, output, input);
    }
}
