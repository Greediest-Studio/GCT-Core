package com.smd.gctcore.misc;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.blocks.botania.BlockGctManaPool;
import com.smd.gctcore.common.blocks.botania.BlockGctManaRock;
import com.smd.gctcore.common.blocks.botania.BlockGctManaSpreader;
import com.smd.gctcore.common.blocks.botania.BlockGctManaWood;
import com.smd.gctcore.common.blocks.nilfheim.BlockGlassrock;
import com.smd.gctcore.common.blocks.nilfheim.BlockMistLotus;
import com.smd.gctcore.common.blocks.nilfheim.BlockNilfheimFluid;
import com.smd.gctcore.common.blocks.nilfheim.BlockNilfheimFence;
import com.smd.gctcore.common.blocks.nilfheim.BlockNilfheimFenceGate;
import com.smd.gctcore.common.blocks.nilfheim.BlockNilfheimLeaves;
import com.smd.gctcore.common.blocks.nilfheim.BlockNilfheimLog;
import com.smd.gctcore.common.blocks.nilfheim.BlockNilfheimPlant;
import com.smd.gctcore.common.blocks.nilfheim.BlockNilfheimPlanks;
import com.smd.gctcore.common.blocks.nilfheim.BlockNilfheimSlab;
import com.smd.gctcore.common.blocks.nilfheim.BlockNilfheimSoil;
import com.smd.gctcore.common.blocks.nilfheim.BlockNilfheimStone;
import com.smd.gctcore.common.blocks.nilfheim.BlockNilfheimStairs;
import com.smd.gctcore.common.blocks.nilfheim.BlockNilfheimVine;
import com.smd.gctcore.common.blocks.nilfheim.BlockNilfheimWall;
import com.smd.gctcore.common.blocks.nilfheim.BlockNilfheimPortalCore;
import com.smd.gctcore.common.blocks.nilfheim.BlockShadowberryBush;
import com.smd.gctcore.common.blocks.nilfheim.BlockSoulIce;
import com.smd.gctcore.common.blocks.arcanearchives.RadiantResonatorBlock;
import com.smd.gctcore.common.blocks.arcanearchives.RawQuartzClusterBlock;
import com.smd.gctcore.common.blocks.arcanearchives.StorageRawQuartzBlock;
import com.smd.gctcore.common.blocks.arcanearchives.StorageShapedQuartzBlock;
import com.smd.gctcore.common.items.ItemBlockNilfheimWall;
import com.smd.gctcore.common.items.ItemBlockGctManaTiered;
import com.smd.gctcore.common.items.ItemBlockGctManaWood;
import com.smd.gctcore.common.tile.NilfheimPortalTileEntity;
import com.smd.gctcore.common.tile.RadiantResonatorTileEntity;
import com.smd.gctcore.common.tile.botania.TileGctManaPool;
import com.smd.gctcore.common.tile.botania.TileGctManaSpreader;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockRegistry {
    public static RadiantResonatorBlock RADIANT_RESONATOR;
    public static RawQuartzClusterBlock RAW_QUARTZ_CLUSTER;
    public static StorageRawQuartzBlock STORAGE_RAW_QUARTZ;
    public static StorageShapedQuartzBlock STORAGE_SHAPED_QUARTZ;
    public static Fluid PRIMORDIAL_FLUID;
    public static BlockNilfheimFluid PRIMORDIAL_LIQUID;
    public static BlockNilfheimStone RIMESTEEL_STONE;
    public static BlockNilfheimStone BLACK_RIME_STONE;
    public static BlockNilfheimStone MIST_CRYSTAL_STONE;
    public static BlockNilfheimStone APATHY_ORE;
    public static BlockGlassrock GLASSROCK;
    public static BlockNilfheimStone ANCIENT_RUNESTONE;
    public static BlockNilfheimSoil EROSION_MOSS;
    public static BlockNilfheimSoil PERMAFROST;
    public static BlockNilfheimSoil ANCIENT_SNOW;
    public static BlockNilfheimSoil ASHEN_SOIL;
    public static BlockNilfheimSoil BOILING_MUD;
    public static BlockSoulIce SOUL_ICE;
    public static BlockNilfheimLog MISTWOOD_LOG;
    public static BlockNilfheimLog DARKRUNED_LOG;
    public static BlockNilfheimLog SNOWPINE_LOG;
    public static BlockNilfheimPlanks MISTWOOD_PLANKS;
    public static BlockNilfheimPlanks SNOWPINE_PLANKS;
    public static BlockNilfheimSlab.Half MISTWOOD_SLAB;
    public static BlockNilfheimSlab.Double MISTWOOD_DOUBLE_SLAB;
    public static BlockNilfheimSlab.Half SNOWPINE_SLAB;
    public static BlockNilfheimSlab.Double SNOWPINE_DOUBLE_SLAB;
    public static BlockNilfheimStairs MISTWOOD_STAIRS;
    public static BlockNilfheimStairs SNOWPINE_STAIRS;
    public static BlockNilfheimFence MISTWOOD_FENCE;
    public static BlockNilfheimFence SNOWPINE_FENCE;
    public static BlockNilfheimFenceGate MISTWOOD_FENCE_GATE;
    public static BlockNilfheimFenceGate SNOWPINE_FENCE_GATE;
    public static BlockNilfheimStone POLISHED_RIMESTEEL;
    public static BlockNilfheimStone RIMESTEEL_BRICKS;
    public static BlockNilfheimStone CRACKED_RIMESTEEL_BRICKS;
    public static BlockNilfheimStone CHISELED_RIMESTEEL;
    public static BlockNilfheimSlab.Half RIMESTEEL_SLAB;
    public static BlockNilfheimSlab.Double RIMESTEEL_DOUBLE_SLAB;
    public static BlockNilfheimStairs RIMESTEEL_STAIRS;
    public static BlockNilfheimWall RIMESTEEL_WALL;
    public static BlockNilfheimStone POLISHED_BLACK_RIME;
    public static BlockNilfheimStone BLACK_RIME_BRICKS;
    public static BlockNilfheimStone CRACKED_BLACK_RIME_BRICKS;
    public static BlockNilfheimStone CHISELED_BLACK_RIME;
    public static BlockNilfheimSlab.Half BLACK_RIME_SLAB;
    public static BlockNilfheimSlab.Double BLACK_RIME_DOUBLE_SLAB;
    public static BlockNilfheimStairs BLACK_RIME_STAIRS;
    public static BlockNilfheimWall BLACK_RIME_WALL;
    public static BlockNilfheimLeaves MISTWOOD_LEAVES;
    public static BlockNilfheimLeaves SNOWPINE_LEAVES;
    public static BlockNilfheimPlant MIST_FERN;
    public static BlockNilfheimVine FROSTBOUND_VINE;
    public static BlockNilfheimPlant WEEPING_ICE_FLOWER;
    public static BlockMistLotus MIST_LOTUS;
    public static BlockNilfheimPlant SOULFIRE_GRASS;
    public static BlockNilfheimPlant ASHEN_SHRUB;
    public static BlockNilfheimVine GLOWING_CREEPER;
    public static BlockShadowberryBush SHADOWBERRY_BUSH;
    public static BlockNilfheimPlant ASHEN_MUSHROOM;
    public static BlockNilfheimPortalCore NILFHEIM_PORTAL_CORE;
    public static BlockGctManaRock GCT_MANA_ROCK;
    public static BlockGctManaPool GCT_MANA_POOL;
    public static BlockGctManaSpreader GCT_MANA_SPREADER;
    public static BlockGctManaWood GCT_MANA_WOOD;

    public static void init() {
        FluidRegistry.enableUniversalBucket();

        PRIMORDIAL_FLUID = new Fluid("primordial_liquid",
                new ResourceLocation("minecraft:blocks/water_still"),
                new ResourceLocation("minecraft:blocks/water_flow"),
                0xB0488A9A)
                .setDensity(1300)
                .setViscosity(1600)
                .setTemperature(260)
                .setLuminosity(2)
                .setUnlocalizedName("gctcore.primordial_liquid");
        FluidRegistry.registerFluid(PRIMORDIAL_FLUID);
        FluidRegistry.addBucketForFluid(PRIMORDIAL_FLUID);

        RADIANT_RESONATOR = new RadiantResonatorBlock();
        RAW_QUARTZ_CLUSTER = new RawQuartzClusterBlock();
        STORAGE_RAW_QUARTZ = new StorageRawQuartzBlock();
        STORAGE_SHAPED_QUARTZ = new StorageShapedQuartzBlock();
        PRIMORDIAL_LIQUID = new BlockNilfheimFluid(PRIMORDIAL_FLUID, "primordial_liquid");
        PRIMORDIAL_FLUID.setBlock(PRIMORDIAL_LIQUID);
        RIMESTEEL_STONE = new BlockNilfheimStone("rimesteel_stone", 2.4F, 12.0F);
        BLACK_RIME_STONE = new BlockNilfheimStone("black_rime_stone", 2.8F, 15.0F);
        MIST_CRYSTAL_STONE = new BlockNilfheimStone("mist_crystal_stone", 1.8F, 8.0F);
        MIST_CRYSTAL_STONE.setLightLevel(0.45F);
        APATHY_ORE = new BlockNilfheimStone("apathy_ore", 30.0F, 20000.0F);
        GLASSROCK = new BlockGlassrock();
        ANCIENT_RUNESTONE = new BlockNilfheimStone("ancient_runestone", 3.0F, 20.0F);
        EROSION_MOSS = new BlockNilfheimSoil("erosion_moss", Material.GRASS, 0.6F);
        PERMAFROST = new BlockNilfheimSoil("permafrost", Material.GROUND, 0.7F);
        ANCIENT_SNOW = new BlockNilfheimSoil("ancient_snow", Material.CRAFTED_SNOW, 0.4F);
        ASHEN_SOIL = new BlockNilfheimSoil("ashen_soil", Material.GROUND, 0.5F);
        BOILING_MUD = new BlockNilfheimSoil("boiling_mud", Material.CLAY, 0.8F);
        SOUL_ICE = new BlockSoulIce();
        MISTWOOD_LOG = new BlockNilfheimLog("mistwood_log");
        DARKRUNED_LOG = new BlockNilfheimLog("darkruned_log");
        SNOWPINE_LOG = new BlockNilfheimLog("snowpine_log");
        MISTWOOD_PLANKS = new BlockNilfheimPlanks("mistwood_planks");
        SNOWPINE_PLANKS = new BlockNilfheimPlanks("snowpine_planks");
        MISTWOOD_SLAB = new BlockNilfheimSlab.Half("mistwood_slab", Material.WOOD, SoundType.WOOD, "axe", 0);
        MISTWOOD_DOUBLE_SLAB = new BlockNilfheimSlab.Double("mistwood_double_slab", Material.WOOD, SoundType.WOOD, "axe", 0);
        SNOWPINE_SLAB = new BlockNilfheimSlab.Half("snowpine_slab", Material.WOOD, SoundType.WOOD, "axe", 0);
        SNOWPINE_DOUBLE_SLAB = new BlockNilfheimSlab.Double("snowpine_double_slab", Material.WOOD, SoundType.WOOD, "axe", 0);
        MISTWOOD_STAIRS = new BlockNilfheimStairs("mistwood_stairs", MISTWOOD_PLANKS.getDefaultState(), SoundType.WOOD, "axe", 0);
        SNOWPINE_STAIRS = new BlockNilfheimStairs("snowpine_stairs", SNOWPINE_PLANKS.getDefaultState(), SoundType.WOOD, "axe", 0);
        MISTWOOD_FENCE = new BlockNilfheimFence("mistwood_fence");
        SNOWPINE_FENCE = new BlockNilfheimFence("snowpine_fence");
        MISTWOOD_FENCE_GATE = new BlockNilfheimFenceGate("mistwood_fence_gate");
        SNOWPINE_FENCE_GATE = new BlockNilfheimFenceGate("snowpine_fence_gate");
        POLISHED_RIMESTEEL = new BlockNilfheimStone("polished_rimesteel", 2.4F, 12.0F);
        RIMESTEEL_BRICKS = new BlockNilfheimStone("rimesteel_bricks", 2.4F, 12.0F);
        CRACKED_RIMESTEEL_BRICKS = new BlockNilfheimStone("cracked_rimesteel_bricks", 2.4F, 12.0F);
        CHISELED_RIMESTEEL = new BlockNilfheimStone("chiseled_rimesteel", 2.4F, 12.0F);
        RIMESTEEL_SLAB = new BlockNilfheimSlab.Half("rimesteel_slab", Material.ROCK, SoundType.STONE, "pickaxe", 13);
        RIMESTEEL_DOUBLE_SLAB = new BlockNilfheimSlab.Double("rimesteel_double_slab", Material.ROCK, SoundType.STONE, "pickaxe", 13);
        RIMESTEEL_STAIRS = new BlockNilfheimStairs("rimesteel_stairs", RIMESTEEL_STONE.getDefaultState(), SoundType.STONE, "pickaxe", 13);
        RIMESTEEL_WALL = new BlockNilfheimWall("rimesteel_wall", RIMESTEEL_STONE);
        POLISHED_BLACK_RIME = new BlockNilfheimStone("polished_black_rime", 2.8F, 15.0F);
        BLACK_RIME_BRICKS = new BlockNilfheimStone("black_rime_bricks", 2.8F, 15.0F);
        CRACKED_BLACK_RIME_BRICKS = new BlockNilfheimStone("cracked_black_rime_bricks", 2.8F, 15.0F);
        CHISELED_BLACK_RIME = new BlockNilfheimStone("chiseled_black_rime", 2.8F, 15.0F);
        BLACK_RIME_SLAB = new BlockNilfheimSlab.Half("black_rime_slab", Material.ROCK, SoundType.STONE, "pickaxe", 13);
        BLACK_RIME_DOUBLE_SLAB = new BlockNilfheimSlab.Double("black_rime_double_slab", Material.ROCK, SoundType.STONE, "pickaxe", 13);
        BLACK_RIME_STAIRS = new BlockNilfheimStairs("black_rime_stairs", BLACK_RIME_STONE.getDefaultState(), SoundType.STONE, "pickaxe", 13);
        BLACK_RIME_WALL = new BlockNilfheimWall("black_rime_wall", BLACK_RIME_STONE);
        MISTWOOD_LEAVES = new BlockNilfheimLeaves("mistwood_leaves");
        MISTWOOD_LEAVES.setLightLevel(0.18F);
        SNOWPINE_LEAVES = new BlockNilfheimLeaves("snowpine_leaves");
        MIST_FERN = new BlockNilfheimPlant("mist_fern", 0.0F);
        FROSTBOUND_VINE = new BlockNilfheimVine("frostbound_vine", 0.0F);
        WEEPING_ICE_FLOWER = new BlockNilfheimPlant("weeping_ice_flower", 0.25F);
        MIST_LOTUS = new BlockMistLotus();
        SOULFIRE_GRASS = new BlockNilfheimPlant("soulfire_grass", 0.45F);
        ASHEN_SHRUB = new BlockNilfheimPlant("ashen_shrub", 0.0F);
        GLOWING_CREEPER = new BlockNilfheimVine("glowing_creeper", 0.5F);
        SHADOWBERRY_BUSH = new BlockShadowberryBush();
        ASHEN_MUSHROOM = new BlockNilfheimPlant("ashen_mushroom", 0.2F);
        NILFHEIM_PORTAL_CORE = new BlockNilfheimPortalCore();
        GCT_MANA_ROCK = new BlockGctManaRock();
        GCT_MANA_POOL = new BlockGctManaPool();
        GCT_MANA_SPREADER = new BlockGctManaSpreader();
        GCT_MANA_WOOD = new BlockGctManaWood();
    }

    public static void registerTileEntities() {
        GameRegistry.registerTileEntity(RadiantResonatorTileEntity.class, new ResourceLocation(Tags.MOD_ID, "radiant_resonator"));
        GameRegistry.registerTileEntity(NilfheimPortalTileEntity.class, new ResourceLocation(Tags.MOD_ID, "nilfheim_portal_core"));
        GameRegistry.registerTileEntity(TileGctManaPool.class, new ResourceLocation(Tags.MOD_ID, "gct_mana_pool"));
        GameRegistry.registerTileEntity(TileGctManaSpreader.class, new ResourceLocation(Tags.MOD_ID, "gct_mana_spreader"));
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
                RADIANT_RESONATOR,
                RAW_QUARTZ_CLUSTER,
                STORAGE_RAW_QUARTZ,
                STORAGE_SHAPED_QUARTZ,
                PRIMORDIAL_LIQUID,
                RIMESTEEL_STONE,
                BLACK_RIME_STONE,
                MIST_CRYSTAL_STONE,
                APATHY_ORE,
                GLASSROCK,
                ANCIENT_RUNESTONE,
                EROSION_MOSS,
                PERMAFROST,
                ANCIENT_SNOW,
                ASHEN_SOIL,
                BOILING_MUD,
                SOUL_ICE,
                MISTWOOD_LOG,
                DARKRUNED_LOG,
                SNOWPINE_LOG,
                MISTWOOD_PLANKS,
                SNOWPINE_PLANKS,
                MISTWOOD_SLAB,
                MISTWOOD_DOUBLE_SLAB,
                SNOWPINE_SLAB,
                SNOWPINE_DOUBLE_SLAB,
                MISTWOOD_STAIRS,
                SNOWPINE_STAIRS,
                MISTWOOD_FENCE,
                SNOWPINE_FENCE,
                MISTWOOD_FENCE_GATE,
                SNOWPINE_FENCE_GATE,
                POLISHED_RIMESTEEL,
                RIMESTEEL_BRICKS,
                CRACKED_RIMESTEEL_BRICKS,
                CHISELED_RIMESTEEL,
                RIMESTEEL_SLAB,
                RIMESTEEL_DOUBLE_SLAB,
                RIMESTEEL_STAIRS,
                RIMESTEEL_WALL,
                POLISHED_BLACK_RIME,
                BLACK_RIME_BRICKS,
                CRACKED_BLACK_RIME_BRICKS,
                CHISELED_BLACK_RIME,
                BLACK_RIME_SLAB,
                BLACK_RIME_DOUBLE_SLAB,
                BLACK_RIME_STAIRS,
                BLACK_RIME_WALL,
                MISTWOOD_LEAVES,
                SNOWPINE_LEAVES,
                MIST_FERN,
                FROSTBOUND_VINE,
                WEEPING_ICE_FLOWER,
                MIST_LOTUS,
                SOULFIRE_GRASS,
                ASHEN_SHRUB,
                GLOWING_CREEPER,
                SHADOWBERRY_BUSH,
                ASHEN_MUSHROOM,
                NILFHEIM_PORTAL_CORE,
                GCT_MANA_ROCK,
                GCT_MANA_POOL,
                GCT_MANA_SPREADER,
                GCT_MANA_WOOD
        );
    }

    @SubscribeEvent
    public void registerBiomes(RegistryEvent.Register<net.minecraft.world.biome.Biome> event) {
        com.smd.gctcore.common.world.biome.nilfheim.NilfheimBiomes.register(event);
    }

    @SubscribeEvent
    public void registerItemBlocks(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                new ItemBlock(RADIANT_RESONATOR).setRegistryName(RADIANT_RESONATOR.getRegistryName()),
                new ItemBlock(RAW_QUARTZ_CLUSTER).setRegistryName(RAW_QUARTZ_CLUSTER.getRegistryName()),
                new ItemBlock(STORAGE_RAW_QUARTZ).setRegistryName(STORAGE_RAW_QUARTZ.getRegistryName()),
                new ItemBlock(STORAGE_SHAPED_QUARTZ).setRegistryName(STORAGE_SHAPED_QUARTZ.getRegistryName()),
                new ItemBlock(RIMESTEEL_STONE).setRegistryName(RIMESTEEL_STONE.getRegistryName()),
                new ItemBlock(POLISHED_RIMESTEEL).setRegistryName(POLISHED_RIMESTEEL.getRegistryName()),
                new ItemBlock(RIMESTEEL_BRICKS).setRegistryName(RIMESTEEL_BRICKS.getRegistryName()),
                new ItemBlock(CRACKED_RIMESTEEL_BRICKS).setRegistryName(CRACKED_RIMESTEEL_BRICKS.getRegistryName()),
                new ItemBlock(CHISELED_RIMESTEEL).setRegistryName(CHISELED_RIMESTEEL.getRegistryName()),
                new ItemSlab(RIMESTEEL_SLAB, RIMESTEEL_SLAB, RIMESTEEL_DOUBLE_SLAB).setRegistryName(RIMESTEEL_SLAB.getRegistryName()),
                new ItemBlock(RIMESTEEL_STAIRS).setRegistryName(RIMESTEEL_STAIRS.getRegistryName()),
                new ItemBlockNilfheimWall(RIMESTEEL_WALL).setRegistryName(RIMESTEEL_WALL.getRegistryName()),
                new ItemBlock(BLACK_RIME_STONE).setRegistryName(BLACK_RIME_STONE.getRegistryName()),
                new ItemBlock(POLISHED_BLACK_RIME).setRegistryName(POLISHED_BLACK_RIME.getRegistryName()),
                new ItemBlock(BLACK_RIME_BRICKS).setRegistryName(BLACK_RIME_BRICKS.getRegistryName()),
                new ItemBlock(CRACKED_BLACK_RIME_BRICKS).setRegistryName(CRACKED_BLACK_RIME_BRICKS.getRegistryName()),
                new ItemBlock(CHISELED_BLACK_RIME).setRegistryName(CHISELED_BLACK_RIME.getRegistryName()),
                new ItemSlab(BLACK_RIME_SLAB, BLACK_RIME_SLAB, BLACK_RIME_DOUBLE_SLAB).setRegistryName(BLACK_RIME_SLAB.getRegistryName()),
                new ItemBlock(BLACK_RIME_STAIRS).setRegistryName(BLACK_RIME_STAIRS.getRegistryName()),
                new ItemBlockNilfheimWall(BLACK_RIME_WALL).setRegistryName(BLACK_RIME_WALL.getRegistryName()),
                new ItemBlock(MIST_CRYSTAL_STONE).setRegistryName(MIST_CRYSTAL_STONE.getRegistryName()),
                new ItemBlock(APATHY_ORE).setRegistryName(APATHY_ORE.getRegistryName()),
                new ItemBlock(GLASSROCK).setRegistryName(GLASSROCK.getRegistryName()),
                new ItemBlock(ANCIENT_RUNESTONE).setRegistryName(ANCIENT_RUNESTONE.getRegistryName()),
                new ItemBlock(EROSION_MOSS).setRegistryName(EROSION_MOSS.getRegistryName()),
                new ItemBlock(PERMAFROST).setRegistryName(PERMAFROST.getRegistryName()),
                new ItemBlock(ANCIENT_SNOW).setRegistryName(ANCIENT_SNOW.getRegistryName()),
                new ItemBlock(ASHEN_SOIL).setRegistryName(ASHEN_SOIL.getRegistryName()),
                new ItemBlock(BOILING_MUD).setRegistryName(BOILING_MUD.getRegistryName()),
                new ItemBlock(SOUL_ICE).setRegistryName(SOUL_ICE.getRegistryName()),
                new ItemBlock(MISTWOOD_LOG).setRegistryName(MISTWOOD_LOG.getRegistryName()),
                new ItemBlock(DARKRUNED_LOG).setRegistryName(DARKRUNED_LOG.getRegistryName()),
                new ItemBlock(SNOWPINE_LOG).setRegistryName(SNOWPINE_LOG.getRegistryName()),
                new ItemBlock(MISTWOOD_PLANKS).setRegistryName(MISTWOOD_PLANKS.getRegistryName()),
                new ItemBlock(SNOWPINE_PLANKS).setRegistryName(SNOWPINE_PLANKS.getRegistryName()),
                new ItemSlab(MISTWOOD_SLAB, MISTWOOD_SLAB, MISTWOOD_DOUBLE_SLAB).setRegistryName(MISTWOOD_SLAB.getRegistryName()),
                new ItemSlab(SNOWPINE_SLAB, SNOWPINE_SLAB, SNOWPINE_DOUBLE_SLAB).setRegistryName(SNOWPINE_SLAB.getRegistryName()),
                new ItemBlock(MISTWOOD_STAIRS).setRegistryName(MISTWOOD_STAIRS.getRegistryName()),
                new ItemBlock(SNOWPINE_STAIRS).setRegistryName(SNOWPINE_STAIRS.getRegistryName()),
                new ItemBlock(MISTWOOD_FENCE).setRegistryName(MISTWOOD_FENCE.getRegistryName()),
                new ItemBlock(SNOWPINE_FENCE).setRegistryName(SNOWPINE_FENCE.getRegistryName()),
                new ItemBlock(MISTWOOD_FENCE_GATE).setRegistryName(MISTWOOD_FENCE_GATE.getRegistryName()),
                new ItemBlock(SNOWPINE_FENCE_GATE).setRegistryName(SNOWPINE_FENCE_GATE.getRegistryName()),
                new ItemBlock(MISTWOOD_LEAVES).setRegistryName(MISTWOOD_LEAVES.getRegistryName()),
                new ItemBlock(SNOWPINE_LEAVES).setRegistryName(SNOWPINE_LEAVES.getRegistryName()),
                new ItemBlock(MIST_FERN).setRegistryName(MIST_FERN.getRegistryName()),
                new ItemBlock(FROSTBOUND_VINE).setRegistryName(FROSTBOUND_VINE.getRegistryName()),
                new ItemBlock(WEEPING_ICE_FLOWER).setRegistryName(WEEPING_ICE_FLOWER.getRegistryName()),
                new ItemBlock(MIST_LOTUS).setRegistryName(MIST_LOTUS.getRegistryName()),
                new ItemBlock(SOULFIRE_GRASS).setRegistryName(SOULFIRE_GRASS.getRegistryName()),
                new ItemBlock(ASHEN_SHRUB).setRegistryName(ASHEN_SHRUB.getRegistryName()),
                new ItemBlock(GLOWING_CREEPER).setRegistryName(GLOWING_CREEPER.getRegistryName()),
                new ItemBlock(SHADOWBERRY_BUSH).setRegistryName(SHADOWBERRY_BUSH.getRegistryName()),
                new ItemBlock(ASHEN_MUSHROOM).setRegistryName(ASHEN_MUSHROOM.getRegistryName()),
                new ItemBlock(NILFHEIM_PORTAL_CORE).setRegistryName(NILFHEIM_PORTAL_CORE.getRegistryName()),
                new ItemBlockGctManaTiered(GCT_MANA_ROCK).setRegistryName(GCT_MANA_ROCK.getRegistryName()),
                new ItemBlockGctManaTiered(GCT_MANA_POOL).setRegistryName(GCT_MANA_POOL.getRegistryName()),
                new ItemBlockGctManaTiered(GCT_MANA_SPREADER).setRegistryName(GCT_MANA_SPREADER.getRegistryName()),
                new ItemBlockGctManaWood(GCT_MANA_WOOD).setRegistryName(GCT_MANA_WOOD.getRegistryName())
        );
        registerOreDictionary();
    }

    private void registerOreDictionary() {
        registerWoodOres(MISTWOOD_LOG, MISTWOOD_PLANKS, MISTWOOD_SLAB, MISTWOOD_STAIRS, MISTWOOD_FENCE, MISTWOOD_FENCE_GATE);
        registerWoodOres(SNOWPINE_LOG, SNOWPINE_PLANKS, SNOWPINE_SLAB, SNOWPINE_STAIRS, SNOWPINE_FENCE, SNOWPINE_FENCE_GATE);
        OreDictionary.registerOre("oreApathy", new ItemStack(APATHY_ORE));
        OreDictionary.registerOre("ingotApathy", new ItemStack(ItemRegistry.APATHY_INGOT));
    }

    private void registerWoodOres(Block log, Block planks, Block slab, Block stairs, Block fence, Block fenceGate) {
        OreDictionary.registerOre("logWood", new ItemStack(log));
        OreDictionary.registerOre("plankWood", new ItemStack(planks));
        OreDictionary.registerOre("slabWood", new ItemStack(slab));
        OreDictionary.registerOre("stairWood", new ItemStack(stairs));
        OreDictionary.registerOre("fenceWood", new ItemStack(fence));
        OreDictionary.registerOre("fenceGateWood", new ItemStack(fenceGate));
    }
}
