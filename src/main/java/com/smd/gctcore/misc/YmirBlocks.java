package com.smd.gctcore.misc;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.blocks.nilfheim.BlockNilfheimSlab;
import com.smd.gctcore.common.blocks.nilfheim.BlockNilfheimStairs;
import com.smd.gctcore.common.blocks.nilfheim.BlockNilfheimWall;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** The two durable masonry palettes used by Ymir's Hell. */
public final class YmirBlocks {
    public static final int HARVEST_LEVEL = 14;
    public static final float HARDNESS = 300.0F;
    public static final float EXPLOSION_RESISTANCE = 4000.0F;

    public static final class Family {
        public final String prefix;
        public final Block stone;
        public final Block bricks;
        public final Block crackedBricks;
        public final Block chiseled;
        public final Block pillar;
        public final Block stairs;
        public final BlockSlab slab;
        public final BlockSlab doubleSlab;
        public final Block wall;

        private Family(String prefix) {
            this.prefix = prefix;
            stone = cube(prefix);
            bricks = cube(prefix + "_bricks");
            crackedBricks = cube("cracked_" + prefix + "_bricks");
            chiseled = cube("chiseled_" + prefix);
            pillar = add(prefix + "_pillar", new Pillar());
            stairs = add(prefix + "_stairs", new BlockNilfheimStairs(prefix + "_stairs",
                    bricks.getDefaultState(), SoundType.STONE, "pickaxe", HARVEST_LEVEL));
            slab = add(prefix + "_slab", new BlockNilfheimSlab.Half(prefix + "_slab",
                    Material.ROCK, SoundType.STONE, "pickaxe", HARVEST_LEVEL));
            doubleSlab = add(prefix + "_double_slab", new DoubleSlab(prefix + "_double_slab", slab));
            doubleSlab.setCreativeTab(null);
            wall = add(prefix + "_wall", new BlockNilfheimWall(prefix + "_wall", bricks));
        }
    }

    private static final Map<String, Block> BLOCKS = new LinkedHashMap<>();
    private static final List<Family> FAMILIES = new ArrayList<>();
    private static boolean oresRegistered;

    public static void init() {
        if (!FAMILIES.isEmpty()) return;
        FAMILIES.add(new Family("ymir_stone"));
        FAMILIES.add(new Family("dark_ymir_stone"));
    }

    public static Block get(String key) {
        Block block = BLOCKS.get(key);
        if (block == null) throw new IllegalArgumentException("Unknown Ymir building block: " + key);
        return block;
    }

    public static IBlockState state(String key) { return get(key).getDefaultState(); }

    public static List<Family> families() { return Collections.unmodifiableList(FAMILIES); }

    public static Map<String, Block> blocks() { return Collections.unmodifiableMap(BLOCKS); }

    private static Block cube(String key) { return add(key, new Block(Material.ROCK)); }

    private static <T extends Block> T add(String key, T block) {
        if (block.getRegistryName() == null) block.setRegistryName(Tags.MOD_ID, key);
        block.setTranslationKey(Tags.MOD_ID + "." + key);
        block.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        block.setHardness(HARDNESS);
        // 1.12 stores setResistance(x) as x * 3 and returns that field / 5.
        block.setResistance(EXPLOSION_RESISTANCE * 5.0F / 3.0F);
        block.setHarvestLevel("pickaxe", HARVEST_LEVEL);
        BLOCKS.put(key, block);
        return block;
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        for (Block block : BLOCKS.values()) event.getRegistry().register(block);
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        for (Family family : FAMILIES) {
            event.getRegistry().registerAll(item(family.stone), item(family.bricks), item(family.crackedBricks),
                    item(family.chiseled), item(family.pillar), item(family.stairs), item(family.wall),
                    new ItemSlab(family.slab, family.slab, family.doubleSlab)
                            .setRegistryName(family.slab.getRegistryName()));
        }
    }

    private static ItemBlock item(Block block) {
        return (ItemBlock) new ItemBlock(block).setRegistryName(block.getRegistryName());
    }

    public static void registerOreDictionary() {
        if (oresRegistered) return;
        oresRegistered = true;
        for (Family family : FAMILIES) {
            ore(family.prefix.startsWith("dark_") ? "stoneDarkYmir" : "stoneYmir", family.stone);
            ore("stone", family.stone);
            ore("stoneBricks", family.bricks);
            ore("stairStone", family.stairs);
            ore("slabStone", family.slab);
            ore("wallStone", family.wall);
        }
    }

    private static void ore(String key, Block block) {
        OreDictionary.registerOre(key, new ItemStack(block, 1, OreDictionary.WILDCARD_VALUE));
    }

    private static final class Pillar extends BlockRotatedPillar {
        private Pillar() {
            super(Material.ROCK);
            setDefaultState(blockState.getBaseState().withProperty(AXIS, EnumFacing.Axis.Y));
        }
    }

    /** Double slabs are placed by ItemSlab and drop two ordinary half slabs. */
    private static final class DoubleSlab extends BlockNilfheimSlab.Double {
        private final BlockSlab half;

        private DoubleSlab(String name, BlockSlab half) {
            super(name, Material.ROCK, SoundType.STONE, "pickaxe", HARVEST_LEVEL);
            this.half = half;
        }

        @Override
        public Item getItemDropped(IBlockState state, Random random, int fortune) {
            return Item.getItemFromBlock(half);
        }

        @Override
        public ItemStack getItem(World world, BlockPos pos, IBlockState state) { return new ItemStack(half); }

        @Override
        public int quantityDropped(Random random) { return 2; }
    }
}
