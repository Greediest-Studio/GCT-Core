package com.smd.gctcore.common.integration.extendedcrafting;

import com.smd.gctcore.Tags;
import com.smd.gctcore.misc.Mods;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.List;

public final class ExtendedCraftingAutomation {
    private static final BlockExtendedCraftingAutomation[][] BLOCKS =
            new BlockExtendedCraftingAutomation[BlockExtendedCraftingAutomation.Kind.values().length][ExtendedCraftingTier.values().length];
    private static final ItemBlankExtendedPattern[] BLANK = new ItemBlankExtendedPattern[4];
    private static final ItemEncodedExtendedPattern[] ENCODED = new ItemEncodedExtendedPattern[4];
    private static boolean initialized;

    public static boolean enabled() {
        return Mods.AE2.isLoading() && Mods.EXTENDED_CRAFTING.isLoading();
    }

    public static void init() {
        if (initialized || !enabled()) return;
        initialized = true;
        for (ExtendedCraftingTier tier : ExtendedCraftingTier.values()) {
            for (BlockExtendedCraftingAutomation.Kind kind : BlockExtendedCraftingAutomation.Kind.values()) {
                BLOCKS[kind.ordinal()][tier.ordinal()] = new BlockExtendedCraftingAutomation(kind, tier);
            }
            BLANK[tier.ordinal()] = new ItemBlankExtendedPattern(tier);
            ENCODED[tier.ordinal()] = new ItemEncodedExtendedPattern(tier);
        }
    }

    public static void registerTileEntities() {
        if (!enabled()) return;
        GameRegistry.registerTileEntity(TileExtendedMolecularAssembler.class,
                new ResourceLocation(Tags.MOD_ID, "extended_molecular_assembler"));
        GameRegistry.registerTileEntity(TileExtendedInterface.class,
                new ResourceLocation(Tags.MOD_ID, "extended_interface"));
        GameRegistry.registerTileEntity(TileExtendedPatternTerminal.class,
                new ResourceLocation(Tags.MOD_ID, "extended_pattern_terminal"));
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        if (!enabled()) return;
        List<Block> values = new ArrayList<>();
        for (BlockExtendedCraftingAutomation[] blocks : BLOCKS) for (Block block : blocks) values.add(block);
        event.getRegistry().registerAll(values.toArray(new Block[values.size()]));
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        if (!enabled()) return;
        List<Item> values = new ArrayList<>();
        for (BlockExtendedCraftingAutomation[] blocks : BLOCKS) for (Block block : blocks)
            values.add(new ItemBlock(block).setRegistryName(block.getRegistryName()));
        for (Item item : BLANK) values.add(item);
        for (Item item : ENCODED) values.add(item);
        event.getRegistry().registerAll(values.toArray(new Item[values.size()]));
    }

    public static BlockExtendedCraftingAutomation block(BlockExtendedCraftingAutomation.Kind kind,
                                                        ExtendedCraftingTier tier) {
        return BLOCKS[kind.ordinal()][tier.ordinal()];
    }
    public static ItemBlankExtendedPattern blankPattern(ExtendedCraftingTier tier) { return BLANK[tier.ordinal()]; }
    public static ItemEncodedExtendedPattern encodedPattern(ExtendedCraftingTier tier) { return ENCODED[tier.ordinal()]; }

    public ExtendedCraftingAutomation() { }
}
