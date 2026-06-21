package com.smd.gctcore.misc;

import com.smd.gctcore.Tags;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class NilfheimRecipes {
    private NilfheimRecipes() {
    }

    public static void init() {
        registerWoodRecipes("mistwood", BlockRegistry.MISTWOOD_LOG, BlockRegistry.MISTWOOD_PLANKS,
                BlockRegistry.MISTWOOD_SLAB, BlockRegistry.MISTWOOD_STAIRS,
                BlockRegistry.MISTWOOD_FENCE, BlockRegistry.MISTWOOD_FENCE_GATE);
        registerWoodRecipes("snowpine", BlockRegistry.SNOWPINE_LOG, BlockRegistry.SNOWPINE_PLANKS,
                BlockRegistry.SNOWPINE_SLAB, BlockRegistry.SNOWPINE_STAIRS,
                BlockRegistry.SNOWPINE_FENCE, BlockRegistry.SNOWPINE_FENCE_GATE);

        registerStoneRecipes("rimesteel", BlockRegistry.RIMESTEEL_STONE, BlockRegistry.POLISHED_RIMESTEEL,
                BlockRegistry.RIMESTEEL_BRICKS, BlockRegistry.RIMESTEEL_SLAB,
                BlockRegistry.RIMESTEEL_STAIRS, BlockRegistry.RIMESTEEL_WALL,
                BlockRegistry.CHISELED_RIMESTEEL);
        registerStoneRecipes("black_rime", BlockRegistry.BLACK_RIME_STONE, BlockRegistry.POLISHED_BLACK_RIME,
                BlockRegistry.BLACK_RIME_BRICKS, BlockRegistry.BLACK_RIME_SLAB,
                BlockRegistry.BLACK_RIME_STAIRS, BlockRegistry.BLACK_RIME_WALL,
                BlockRegistry.CHISELED_BLACK_RIME);

        GameRegistry.addSmelting(
                BlockRegistry.RIMESTEEL_BRICKS,
                new ItemStack(BlockRegistry.CRACKED_RIMESTEEL_BRICKS),
                0.1F
        );
        GameRegistry.addSmelting(
                BlockRegistry.BLACK_RIME_BRICKS,
                new ItemStack(BlockRegistry.CRACKED_BLACK_RIME_BRICKS),
                0.1F
        );
        GameRegistry.addSmelting(
                BlockRegistry.APATHY_ORE,
                new ItemStack(ItemRegistry.APATHY_INGOT),
                0.7F
        );
    }

    private static void registerWoodRecipes(String prefix, Block log, Block planks, Block slab, Block stairs, Block fence, Block fenceGate) {
        addShaped(prefix + "_planks", new ItemStack(planks, 4), "L", 'L', log);
        addShaped(prefix + "_slab", new ItemStack(slab, 6), "PPP", 'P', planks);
        addShaped(prefix + "_stairs", new ItemStack(stairs, 4), "P  ", "PP ", "PPP", 'P', planks);
        addShaped(prefix + "_fence", new ItemStack(fence, 3), "PSP", "PSP", 'P', planks, 'S', Items.STICK);
        addShaped(prefix + "_fence_gate", new ItemStack(fenceGate), "SPS", "SPS", 'P', planks, 'S', Items.STICK);
    }

    private static void registerStoneRecipes(String prefix, Block stone, Block polished, Block bricks, Block slab, Block stairs, Block wall, Block chiseled) {
        addShaped("polished_" + prefix, new ItemStack(polished, 4), "SS", "SS", 'S', stone);
        addShaped(prefix + "_bricks", new ItemStack(bricks, 4), "PP", "PP", 'P', polished);
        addShaped(prefix + "_slab", new ItemStack(slab, 6), "SSS", 'S', stone);
        addShaped(prefix + "_slab_from_bricks", new ItemStack(slab, 6), "BBB", 'B', bricks);
        addShaped(prefix + "_stairs", new ItemStack(stairs, 4), "S  ", "SS ", "SSS", 'S', stone);
        addShaped(prefix + "_stairs_from_bricks", new ItemStack(stairs, 4), "B  ", "BB ", "BBB", 'B', bricks);
        addShaped(prefix + "_wall", new ItemStack(wall, 6), "SSS", "SSS", 'S', stone);
        addShaped(prefix + "_wall_from_bricks", new ItemStack(wall, 6), "BBB", "BBB", 'B', bricks);
        addShaped("chiseled_" + prefix, new ItemStack(chiseled), "S", "S", 'S', slab);
    }

    private static void addShaped(String name, ItemStack output, Object... recipe) {
        GameRegistry.addShapedRecipe(
                new ResourceLocation(Tags.MOD_ID, name),
                null,
                output,
                recipe
        );
    }
}
