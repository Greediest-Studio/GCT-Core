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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Height-aware natural monster lists; command, egg and spawner summons keep their own rules. */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class JotunheimGiantSpawns {
    private static final double LOCAL_RADIUS = 64.0D;
    private static final double LOCAL_HEIGHT = 24.0D;
    private static final Biome.SpawnListEntry SENTINEL = entry(EntityCloudGiantSentinel.class);
    private static final Biome.SpawnListEntry CROSSBOWMAN = entry(EntityCloudGiantCrossbowman.class);
    private static final Biome.SpawnListEntry HILL = entry(EntityHillGiant.class);
    private static final Biome.SpawnListEntry STONE = entry(EntityStoneGiant.class);
    private static final Biome.SpawnListEntry NOBLE = entry(EntityFomorianNoble.class);
    private static final Biome.SpawnListEntry WARLOCK = entry(EntityFomorianWarlock.class);
    private static final Biome.SpawnListEntry FIRE = entry(EntityFireGiant.class);
    private static final Biome.SpawnListEntry FROST = entry(EntityFrostGiant.class);
    private static final Biome.SpawnListEntry ETTIN = entry(EntityEttin.class);
    private static final List<List<List<Biome.SpawnListEntry>>> LISTS = createLists();

    private JotunheimGiantSpawns() {}

    private static Biome.SpawnListEntry entry(Class<? extends EntityLiving> type) {
        return new Biome.SpawnListEntry(type, 1, 1, 1);
    }

    private static List<List<List<Biome.SpawnListEntry>>> createLists() {
        List<List<List<Biome.SpawnListEntry>>> biomes = new ArrayList<>();
        for (int biome = 0; biome < JotunheimTerrainKernel.BIOME_COUNT; biome++) {
            List<List<Biome.SpawnListEntry>> layers = new ArrayList<>();
            for (JotunheimTerrainKernel.HeightLayer layer : JotunheimTerrainKernel.HeightLayer.values()) {
                List<Biome.SpawnListEntry> entries = new ArrayList<>();
                switch (layer) {
                    case CLOUD: entries.add(SENTINEL); entries.add(CROSSBOWMAN); break;
                    case SURFACE: entries.add(HILL); break;
                    case DEEP: entries.add(STONE); break;
                    case LAVA: entries.add(NOBLE); entries.add(WARLOCK); break;
                    default: break;
                }
                // These are additions to the layer's residents, at every altitude band.
                if (biome == 6) entries.add(FIRE);
                if (biome == 1) entries.add(FROST);
                if (layer != JotunheimTerrainKernel.HeightLayer.CLOUD) entries.add(ETTIN);
                layers.add(Collections.unmodifiableList(entries));
            }
            biomes.add(Collections.unmodifiableList(layers));
        }
        return Collections.unmodifiableList(biomes);
    }

    public static List<Biome.SpawnListEntry> getPossibleCreatures(
            EnumCreatureType type, int biomeIndex, BlockPos pos) {
        if (type != EnumCreatureType.MONSTER || biomeIndex < 0
                || biomeIndex >= JotunheimTerrainKernel.BIOME_COUNT) return Collections.emptyList();
        // An island's top block may occupy the last Y in its band; the mob stands above it.
        JotunheimTerrainKernel.HeightLayer layer = JotunheimTerrainKernel.layerAt(pos.getY() - 1);
        return layer == null ? Collections.emptyList() : LISTS.get(biomeIndex).get(layer.ordinal());
    }

    private static boolean isNaturalGiant(Class<?> type) {
        return type == EntityCloudGiantSentinel.class || type == EntityCloudGiantCrossbowman.class
                || type == EntityHillGiant.class || type == EntityStoneGiant.class
                || type == EntityFomorianNoble.class || type == EntityFomorianWarlock.class
                || type == EntityFireGiant.class || type == EntityFrostGiant.class
                || type == EntityEttin.class;
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
        boolean eligible = false;
        for (Biome.SpawnListEntry entry : getPossibleCreatures(EnumCreatureType.MONSTER,
                ((BiomeJotunheimBase) biome).index(), feet)) {
            if (entry.entityClass == giant.getClass()) { eligible = true; break; }
        }
        if (!eligible || !world.getWorldBorder().contains(feet)) return;
        IBlockState floor = world.getBlockState(feet.down());
        if (!WorldEntitySpawner.canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType.ON_GROUND, world, feet)
                || !floor.canEntitySpawn(giant)) return;

        // Weights only select a species; an independent roll controls actual rarity.
        if (world.rand.nextInt(Math.max(1, JotunheimConfig.giantSpawnRoll)) != 0) return;
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
