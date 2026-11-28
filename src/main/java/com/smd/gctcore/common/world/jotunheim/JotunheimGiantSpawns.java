package com.smd.gctcore.common.world.jotunheim;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.config.JotunheimConfig;
import com.smd.gctcore.common.entity.EntityCloudGiantCrossbowman;
import com.smd.gctcore.common.entity.EntityCloudGiantSentinel;
import com.smd.gctcore.common.entity.EntityEttin;
import com.smd.gctcore.common.entity.EntityFireGiant;
import com.smd.gctcore.common.entity.EntityFomorianNoble;
import com.smd.gctcore.common.entity.EntityFomorianWarlock;
import com.smd.gctcore.common.entity.EntityFrostGiant;
import com.smd.gctcore.common.entity.EntityHillGiant;
import com.smd.gctcore.common.entity.EntityStoneGiant;
import com.smd.gctcore.common.world.biome.jotunheim.BiomeJotunheimBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.entity.living.LivingPackSizeEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Height-aware natural monster lists; command, egg and spawner summons keep their own rules. */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class JotunheimGiantSpawns {
    private static final double LOCAL_RADIUS = 64.0D;
    private static final double LOCAL_HEIGHT = 24.0D;
    private static final int RESIDENT_WEIGHT = 4;
    private static final int ELEMENTAL_WEIGHT = 12;
    private static final int VISITOR_WEIGHT = 1;
    private static final int CYCLOPS_WEIGHT = 4;
    private static final ResourceLocation CYCLOPS_ID = new ResourceLocation("iceandfire", "cyclops");
    private static final Biome.SpawnListEntry SENTINEL = entry(EntityCloudGiantSentinel.class);
    private static final Biome.SpawnListEntry CROSSBOWMAN = entry(EntityCloudGiantCrossbowman.class);
    private static final Biome.SpawnListEntry HILL = entry(EntityHillGiant.class);
    private static final Biome.SpawnListEntry STONE = entry(EntityStoneGiant.class);
    private static final Biome.SpawnListEntry NOBLE = entry(EntityFomorianNoble.class);
    private static final Biome.SpawnListEntry WARLOCK = entry(EntityFomorianWarlock.class);
    private static final Biome.SpawnListEntry FIRE = entry(EntityFireGiant.class, ELEMENTAL_WEIGHT);
    private static final Biome.SpawnListEntry FROST = entry(EntityFrostGiant.class, ELEMENTAL_WEIGHT);
    private static final Biome.SpawnListEntry ETTIN = entry(EntityEttin.class);
    private static final List<List<List<Biome.SpawnListEntry>>> LISTS = createLists();
    private static volatile CyclopsSpawns cyclopsSpawns;

    private JotunheimGiantSpawns() {}

    private static Biome.SpawnListEntry entry(Class<? extends EntityLiving> type) {
        return entry(type, RESIDENT_WEIGHT);
    }

    private static Biome.SpawnListEntry entry(Class<? extends EntityLiving> type, int weight) {
        return new Biome.SpawnListEntry(type, weight, 1, 1);
    }

    private static void addResident(List<Biome.SpawnListEntry> entries, Biome.SpawnListEntry resident,
                                    boolean elementalBiome) {
        entries.add(elementalBiome ? entry(resident.entityClass, VISITOR_WEIGHT) : resident);
    }

    private static boolean isElementalResident(Class<?> type, int biomeIndex) {
        // Fire giants extend frost giants, so use exact classes to keep their habitats separate.
        return biomeIndex == 1 && type == EntityFrostGiant.class
                || biomeIndex == 6 && type == EntityFireGiant.class;
    }

    private static List<List<List<Biome.SpawnListEntry>>> createLists() {
        List<List<List<Biome.SpawnListEntry>>> biomes = new ArrayList<>();
        for (int biome = 0; biome < JotunheimTerrainKernel.BIOME_COUNT; biome++) {
            boolean elementalBiome = biome == 1 || biome == 6;
            List<List<Biome.SpawnListEntry>> layers = new ArrayList<>();
            for (JotunheimTerrainKernel.HeightLayer layer : JotunheimTerrainKernel.HeightLayer.values()) {
                List<Biome.SpawnListEntry> entries = new ArrayList<>();
                switch (layer) {
                    case CLOUD:
                        addResident(entries, SENTINEL, elementalBiome);
                        addResident(entries, CROSSBOWMAN, elementalBiome);
                        break;
                    case SURFACE: addResident(entries, HILL, elementalBiome); break;
                    case DEEP: addResident(entries, STONE, elementalBiome); break;
                    case LAVA:
                        addResident(entries, NOBLE, elementalBiome);
                        addResident(entries, WARLOCK, elementalBiome);
                        break;
                    default: break;
                }
                // Frost/fire giants dominate their own biomes across all four altitude bands.
                if (biome == 6) entries.add(FIRE);
                if (biome == 1) entries.add(FROST);
                if (layer != JotunheimTerrainKernel.HeightLayer.CLOUD) addResident(entries, ETTIN, elementalBiome);
                layers.add(Collections.unmodifiableList(entries));
            }
            biomes.add(Collections.unmodifiableList(layers));
        }
        return Collections.unmodifiableList(biomes);
    }

    public static List<Biome.SpawnListEntry> getPossibleCreatures(
            EnumCreatureType type, int biomeIndex, BlockPos pos) {
        if (type != EnumCreatureType.MONSTER || biomeIndex < 0
                || biomeIndex >= JotunheimTerrainKernel.BIOME_COUNT
                || pos.getY() < 1 || pos.getY() >= 256) return Collections.emptyList();
        // An island's top block may occupy the last Y in its band; the mob stands above it.
        JotunheimTerrainKernel.HeightLayer layer = JotunheimTerrainKernel.layerAt(pos.getY() - 1);
        CyclopsSpawns cyclops = getCyclopsSpawns();
        if (cyclops != null) {
            // Cyclopes may also stand on terrain/structures between the four altitude bands.
            return layer == null ? cyclops.outsideLayers : cyclops.lists.get(biomeIndex).get(layer.ordinal());
        }
        return layer == null ? Collections.emptyList() : LISTS.get(biomeIndex).get(layer.ordinal());
    }

    private static CyclopsSpawns getCyclopsSpawns() {
        CyclopsSpawns cached = cyclopsSpawns;
        if (cached != null) return cached;
        // Resolve only after registration, without loading Ice and Fire classes at startup.
        // Do not cache absence: an early query must not permanently disable this integration.
        EntityEntry registered = ForgeRegistries.ENTITIES.getValue(CYCLOPS_ID);
        if (registered == null || !EntityLiving.class.isAssignableFrom(registered.getEntityClass())) return null;
        synchronized (JotunheimGiantSpawns.class) {
            if (cyclopsSpawns == null) {
                cyclopsSpawns = new CyclopsSpawns(registered.getEntityClass().asSubclass(EntityLiving.class));
            }
            return cyclopsSpawns;
        }
    }

    private static boolean isCyclops(Class<?> type) {
        CyclopsSpawns cyclops = getCyclopsSpawns();
        return cyclops != null && type == cyclops.entityClass;
    }

    private static final class CyclopsSpawns {
        final Class<? extends EntityLiving> entityClass;
        final List<Biome.SpawnListEntry> outsideLayers;
        final List<List<List<Biome.SpawnListEntry>>> lists;

        CyclopsSpawns(Class<? extends EntityLiving> entityClass) {
            this.entityClass = entityClass;
            Biome.SpawnListEntry cyclops = entry(entityClass, CYCLOPS_WEIGHT);
            outsideLayers = Collections.singletonList(cyclops);
            List<List<List<Biome.SpawnListEntry>>> biomes = new ArrayList<>();
            for (List<List<Biome.SpawnListEntry>> biome : LISTS) {
                List<List<Biome.SpawnListEntry>> layers = new ArrayList<>();
                for (List<Biome.SpawnListEntry> layer : biome) {
                    List<Biome.SpawnListEntry> entries = new ArrayList<>(layer);
                    entries.add(cyclops);
                    layers.add(Collections.unmodifiableList(entries));
                }
                biomes.add(Collections.unmodifiableList(layers));
            }
            lists = Collections.unmodifiableList(biomes);
        }
    }

    private static boolean isNaturalGiant(Class<?> type) {
        return type == EntityCloudGiantSentinel.class || type == EntityCloudGiantCrossbowman.class
                || type == EntityHillGiant.class || type == EntityStoneGiant.class
                || type == EntityFomorianNoble.class || type == EntityFomorianWarlock.class
                || type == EntityFireGiant.class || type == EntityFrostGiant.class
                || type == EntityEttin.class || isCyclops(type);
    }

    /** Rare, solitary giants may inhabit bright clouds and luminous undergrowth too. */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
        World world = event.getWorld();
        EntityLiving giant = (EntityLiving) event.getEntityLiving();
        if (world.isRemote || !(world.provider instanceof WorldProviderJotunheim)
                || event.isSpawner() || !isNaturalGiant(giant.getClass())
                || event.getResult() != Event.Result.DEFAULT) return;

        event.setResult(Event.Result.DENY);
        if (world.getDifficulty() == EnumDifficulty.PEACEFUL
                || !world.getGameRules().getBoolean("doMobSpawning")) return;
        AxisAlignedBB box = giant.getEntityBoundingBox();
        // Collision checks inspect neighbouring blocks; never generate chunks from a spawn check.
        if (box.minY < 1.0D || box.maxY > world.getHeight()
                || !world.isAreaLoaded(new BlockPos(box.minX - 1.0D, box.minY - 1.0D, box.minZ - 1.0D),
                new BlockPos(box.maxX + 1.0D, box.maxY + 1.0D, box.maxZ + 1.0D), false)) return;
        BlockPos feet = new BlockPos(giant.posX, box.minY, giant.posZ);
        Biome biome = world.getBiome(feet);
        if (!(biome instanceof BiomeJotunheimBase)) return;
        int biomeIndex = ((BiomeJotunheimBase) biome).index();
        boolean eligible = false;
        for (Biome.SpawnListEntry entry : getPossibleCreatures(EnumCreatureType.MONSTER,
                biomeIndex, feet)) {
            if (entry.entityClass == giant.getClass()) { eligible = true; break; }
        }
        if (!eligible || !world.getWorldBorder().contains(feet)) return;
        IBlockState floor = world.getBlockState(feet.down());
        if (!WorldEntitySpawner.canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType.ON_GROUND, world, feet)
                || !floor.canEntitySpawn(giant)) return;

        // Weights only select a species; an independent roll controls actual rarity.
        boolean cyclops = isCyclops(giant.getClass());
        int spawnRoll = Math.max(1, cyclops ? JotunheimConfig.cyclopsSpawnRoll : JotunheimConfig.giantSpawnRoll);
        if (!cyclops && isElementalResident(giant.getClass(), biomeIndex)) {
            spawnRoll = Math.max(1, (spawnRoll + 3) / 4);
        }
        if (world.rand.nextInt(spawnRoll) != 0) return;
        // Ettins pass only one in four of their formerly eligible natural spawn attempts.
        if (giant instanceof EntityEttin && world.rand.nextInt(4) != 0) return;
        if (!giant.isNotColliding()) return;
        int nearby = world.getEntitiesWithinAABB(EntityLiving.class,
                box.grow(LOCAL_RADIUS, LOCAL_HEIGHT, LOCAL_RADIUS), other -> other != giant
                        && other.isEntityAlive() && isNaturalGiant(other.getClass())
                        && Math.abs(other.posY - giant.posY) <= LOCAL_HEIGHT
                        && (other.posX - giant.posX) * (other.posX - giant.posX)
                        + (other.posZ - giant.posZ) * (other.posZ - giant.posZ) <= LOCAL_RADIUS * LOCAL_RADIUS).size();
        if (nearby >= Math.max(1, JotunheimConfig.giantLocalLimit)) return;
        event.setResult(Event.Result.ALLOW);
    }

    @SubscribeEvent
    public static void onPackSize(LivingPackSizeEvent event) {
        EntityLiving giant = (EntityLiving) event.getEntityLiving();
        if (giant.world.provider instanceof WorldProviderJotunheim && isNaturalGiant(giant.getClass())) {
            // Monster spawning uses this event, rather than SpawnListEntry's min/max group size.
            event.setMaxPackSize(1);
            event.setResult(Event.Result.ALLOW);
        }
    }
}
