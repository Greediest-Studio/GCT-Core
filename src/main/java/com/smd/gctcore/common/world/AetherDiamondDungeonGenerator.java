package com.smd.gctcore.common.world;

import com.gildedgames.the_aether.AetherConfig;
import com.smd.gctcore.gctcore;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureComponentTemplate;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;
import java.util.HashSet;
import java.util.Set;

/** Seed-stable Aether diamond dungeon placement, using the vanilla MapGenStructure lifecycle. */
public final class AetherDiamondDungeonGenerator implements IWorldGenerator {
    private static final String STRUCTURE_NAME = "GCTCoreDiamondDungeon";
    private static final ResourceLocation TEMPLATE_ID = new ResourceLocation("gctcore", "aether_diamond_dungeon");
    private static final int GRID_SIZE = 10;
    private static final int MIN_ABSOLUTE_COORDINATE = 3000;
    private static final int FLOOR_Y = 100;
    private static final int CENTER_LOCAL_X = 77;
    private static final int CENTER_LOCAL_Z = 77;
    private static final long PLACEMENT_SEED_SALT = 0xD1B54A32D192ED03L;

    private static boolean registered;
    private final DiamondDungeonMapGen structure = new DiamondDungeonMapGen();

    public static void register() {
        if (registered) {
            return;
        }
        MapGenStructureIO.registerStructure(DiamondDungeonStart.class, "gctcore:diamond_dungeon_start");
        MapGenStructureIO.registerStructureComponent(DiamondDungeonPiece.class,
                "gctcore:diamond_dungeon_piece");
        GameRegistry.registerWorldGenerator(new AetherDiamondDungeonGenerator(), 0);
        registered = true;
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world,
                         IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (!(world instanceof WorldServer)
                || world.provider.getDimension() != AetherConfig.dimension.aether_dimension_id) {
            return;
        }

        structure.generate(world, chunkX, chunkZ, null);
        structure.generateStructure(world, random, new ChunkPos(chunkX, chunkZ));
    }

    private static final class DiamondDungeonMapGen extends MapGenStructure {
        private World activeWorld;

        @Override
        public void generate(World worldIn, int x, int z, net.minecraft.world.chunk.ChunkPrimer primer) {
            if (activeWorld != worldIn) {
                structureMap.clear();
                structureData = null;
                activeWorld = worldIn;
            }
            super.generate(worldIn, x, z, primer);
        }

        @Override
        public String getStructureName() {
            return STRUCTURE_NAME;
        }

        @Override
        public BlockPos getNearestStructurePos(World worldIn, BlockPos pos, boolean findUnexplored) {
            return null;
        }

        @Override
        protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
            if (Math.floorMod(chunkX, GRID_SIZE) != 0 || Math.floorMod(chunkZ, GRID_SIZE) != 0) {
                return false;
            }

            long centerX = ((long) chunkX << 4) + 8L;
            long centerZ = ((long) chunkZ << 4) + 8L;
            if (Math.abs(centerX) < MIN_ABSOLUTE_COORDINATE
                    || Math.abs(centerZ) < MIN_ABSOLUTE_COORDINATE) {
                return false;
            }

            int primaryChance = Math.max(1, AetherConfig.world_gen.golden_dungeon_primary_chance);
            int secondaryChance = Math.max(1, AetherConfig.world_gen.golden_dungeon_secondary_chance);
            long seed = this.world.getSeed() ^ PLACEMENT_SEED_SALT
                    ^ ((long) chunkX * 341873128712L)
                    ^ ((long) chunkZ * 132897987541L)
                    ^ ((long) this.world.provider.getDimension() * 0x9E3779B97F4A7C15L);
            Random placementRandom = new Random(seed);
            return placementRandom.nextInt(primaryChance) == 0
                    || placementRandom.nextInt(secondaryChance) == 0;
        }

        @Override
        protected StructureStart getStructureStart(int chunkX, int chunkZ) {
            return new DiamondDungeonStart(this.world, this.rand, chunkX, chunkZ);
        }
    }

    public static final class DiamondDungeonStart extends StructureStart {
        private final Set<ChunkPos> processed = new HashSet<>();

        public DiamondDungeonStart() {
        }

        private DiamondDungeonStart(World world, Random random, int chunkX, int chunkZ) {
            super(chunkX, chunkZ);
            random.setSeed(world.getSeed());
            long xSeed = random.nextLong();
            long zSeed = random.nextLong();
            random.setSeed((long) chunkX * xSeed ^ (long) chunkZ * zSeed ^ world.getSeed());

            Rotation rotation = Rotation.values()[random.nextInt(Rotation.values().length)];
            PlacementSettings settings = new PlacementSettings()
                    .setMirror(Mirror.NONE)
                    .setRotation(rotation)
                    .setIgnoreEntities(true);
            BlockPos center = new BlockPos((chunkX << 4) + 8, FLOOR_Y, (chunkZ << 4) + 8);
            BlockPos transformedCenter = Template.transformedBlockPos(settings,
                    new BlockPos(CENTER_LOCAL_X, 0, CENTER_LOCAL_Z));
            BlockPos origin = center.subtract(transformedCenter);

            Template template = loadTemplate(world.getSaveHandler().getStructureTemplateManager());
            if (template == null) {
                gctcore.LOGGER.error("Unable to load diamond dungeon template {}", TEMPLATE_ID);
                return;
            }

            this.components.add(new DiamondDungeonPiece(template, origin, rotation));
            this.updateBoundingBox();
        }

        @Override
        public boolean isValidForPostProcess(ChunkPos chunkPos) {
            return !processed.contains(chunkPos) && super.isValidForPostProcess(chunkPos);
        }

        @Override
        public void notifyPostProcessAt(ChunkPos chunkPos) {
            super.notifyPostProcessAt(chunkPos);
            processed.add(chunkPos);
        }

        @Override
        public void writeToNBT(NBTTagCompound tag) {
            NBTTagList chunks = new NBTTagList();
            for (ChunkPos chunkPos : processed) {
                NBTTagCompound entry = new NBTTagCompound();
                entry.setInteger("X", chunkPos.x);
                entry.setInteger("Z", chunkPos.z);
                chunks.appendTag(entry);
            }
            tag.setTag("Processed", chunks);
        }

        @Override
        public void readFromNBT(NBTTagCompound tag) {
            NBTTagList chunks = tag.getTagList("Processed", 10);
            for (int i = 0; i < chunks.tagCount(); i++) {
                NBTTagCompound entry = chunks.getCompoundTagAt(i);
                processed.add(new ChunkPos(entry.getInteger("X"), entry.getInteger("Z")));
            }
        }
    }

    public static final class DiamondDungeonPiece extends StructureComponentTemplate {
        private Rotation templateRotation = Rotation.NONE;

        public DiamondDungeonPiece() {
        }

        private DiamondDungeonPiece(Template template, BlockPos origin, Rotation rotation) {
            this.templateRotation = rotation;
            PlacementSettings settings = placementSettings(rotation);
            this.setup(template, origin, settings);
        }

        @Override
        public boolean addComponentParts(World worldIn, Random randomIn, StructureBoundingBox chunkBounds) {
            this.placeSettings.setBoundingBox(chunkBounds);
            this.template.addBlocksToWorld(worldIn, this.templatePosition,
                    SKIP_STRUCTURE_VOID, this.placeSettings, 18);
            return true;
        }

        @Override
        protected void handleDataMarker(String function, BlockPos pos, World worldIn,
                                        Random randomIn, StructureBoundingBox chunkBounds) {
        }

        @Override
        protected void writeStructureToNBT(net.minecraft.nbt.NBTTagCompound tagCompound) {
            super.writeStructureToNBT(tagCompound);
            tagCompound.setString("TemplateRotation", this.templateRotation.name());
        }

        @Override
        protected void readStructureFromNBT(net.minecraft.nbt.NBTTagCompound tagCompound,
                                            TemplateManager templateManager) {
            super.readStructureFromNBT(tagCompound, templateManager);
            this.templateRotation = Rotation.valueOf(tagCompound.getString("TemplateRotation"));
            Template template = loadTemplate(templateManager);
            if (template != null) {
                this.setup(template, this.templatePosition, placementSettings(this.templateRotation));
            }
        }
    }

    private static final ITemplateProcessor SKIP_STRUCTURE_VOID = (world, pos, info) ->
            info.blockState.getBlock() == Blocks.STRUCTURE_VOID ? null : info;

    private static PlacementSettings placementSettings(Rotation rotation) {
        return new PlacementSettings()
                .setMirror(Mirror.NONE)
                .setRotation(rotation)
                .setIgnoreEntities(true);
    }

    private static Template loadTemplate(TemplateManager manager) {
        try {
            return manager.getTemplate(null, TEMPLATE_ID);
        } catch (RuntimeException exception) {
            gctcore.LOGGER.error("Failed to read diamond dungeon template {}", TEMPLATE_ID, exception);
            return null;
        }
    }

    private AetherDiamondDungeonGenerator() {
    }
}
