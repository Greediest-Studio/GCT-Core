package com.smd.gctcore.misc;

import com.smd.gctcore.common.blocks.jotunheim.BlockJotunheimDoubleWoodSlab;
import com.smd.gctcore.common.blocks.nilfheim.BlockNilfheimFence;
import com.smd.gctcore.common.blocks.nilfheim.BlockNilfheimFenceGate;
import com.smd.gctcore.common.blocks.nilfheim.BlockNilfheimPlanks;
import com.smd.gctcore.common.blocks.nilfheim.BlockNilfheimSlab;
import com.smd.gctcore.common.blocks.nilfheim.BlockNilfheimStairs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Building sets for all of Jotunheim's natural log and stem blocks. */
public final class JotunheimWoodBlocks {
    public static final class Family {
        public final String key;
        public final String logKey;
        public final Block planks;
        public final BlockSlab slab;
        public final BlockSlab doubleSlab;
        public final Block stairs;
        public final Block fence;
        public final Block fenceGate;

        private Family(String key, String logKey, String registryPrefix) {
            this.key = key;
            this.logKey = logKey;
            String prefix = "jotunheim_" + registryPrefix;
            planks = new BlockNilfheimPlanks(prefix + "_planks");
            slab = new BlockNilfheimSlab.Half(prefix + "_slab", Material.WOOD, SoundType.WOOD, "axe", 0);
            doubleSlab = new BlockJotunheimDoubleWoodSlab(prefix + "_double_slab", slab);
            stairs = new BlockNilfheimStairs(prefix + "_stairs", planks.getDefaultState(), SoundType.WOOD, "axe", 0);
            fence = new BlockNilfheimFence(prefix + "_fence");
            fenceGate = new BlockNilfheimFenceGate(prefix + "_fence_gate");
        }

        public Block log() { return JotunheimBlocks.get(logKey); }
    }

    private static final List<Family> FAMILIES = new ArrayList<>();
    private static boolean oresRegistered;

    public static void init() {
        if (!FAMILIES.isEmpty()) return;
        FAMILIES.add(new Family("jotunn_oak", "jotunn_oak_log", "oak"));
        FAMILIES.add(new Family("elder_pine", "elder_pine_log", "elder_pine"));
        FAMILIES.add(new Family("sunkenwood", "sunkenwood_log", "sunkenwood"));
        FAMILIES.add(new Family("charred", "charred_log", "charred"));
        FAMILIES.add(new Family("lightning_struck", "lightning_struck_log", "lightning_struck"));
        FAMILIES.add(new Family("ancient_root", "ancient_root", "ancient_root"));
        FAMILIES.add(new Family("fungal", "fungal_stem", "fungal"));
    }

    public static List<Family> families() { return Collections.unmodifiableList(FAMILIES); }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        for (Family family : FAMILIES) {
            event.getRegistry().registerAll(family.planks, family.slab, family.doubleSlab,
                    family.stairs, family.fence, family.fenceGate);
        }
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        for (Family family : FAMILIES) {
            event.getRegistry().registerAll(
                    item(family.planks),
                    new ItemSlab(family.slab, family.slab, family.doubleSlab).setRegistryName(family.slab.getRegistryName()),
                    item(family.stairs), item(family.fence), item(family.fenceGate));
        }
    }

    private static ItemBlock item(Block block) {
        return (ItemBlock) new ItemBlock(block).setRegistryName(block.getRegistryName());
    }

    /** Called in init, once every participating ItemBlock has been registered. */
    public static void registerOreDictionary() {
        if (oresRegistered) return;
        oresRegistered = true;
        for (Family family : FAMILIES) {
            ore("logWood", family.log());
            ore("plankWood", family.planks);
            ore("slabWood", family.slab);
            ore("stairWood", family.stairs);
            ore("fenceWood", family.fence);
            ore("fenceGateWood", family.fenceGate);
        }
        ore("logWood", JotunheimBlocks.get("jotunn_oak_bark"));
        ore("logWood", JotunheimBlocks.get("elder_pine_bark"));
        ore("logWood", JotunheimBlocks.get("sunkenwood_bark"));
    }

    private static void ore(String name, Block block) {
        // All log axes, slab halves and directional states represent the same material.
        OreDictionary.registerOre(name, new ItemStack(block, 1, OreDictionary.WILDCARD_VALUE));
    }
}
