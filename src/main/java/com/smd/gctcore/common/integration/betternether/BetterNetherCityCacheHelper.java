package com.smd.gctcore.common.integration.betternether;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.config.GCTMixinConfig;
import com.smd.gctcore.gctcore;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.io.File;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Random;

/**
 * Manages lazy instantiation and soft-reference caching for BetterNether's
 * {@code BNWorldGenerator.cityManager} (~150-185 MiB of structure templates).
 */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class BetterNetherCityCacheHelper {

    private static final long RANDOM_A = 341873128712L;
    private static final long RANDOM_B = 132897987541L;
    private static final long IDLE_DEMOTE_MS = 15_000L;

    private static final Object LOCK = new Object();

    private static volatile boolean handlesResolved = false;
    private static volatile boolean handlesAvailable = false;
    private static Field cityManagerField;
    private static Field noiseField;
    private static Constructor<?> cityManagerCtor;
    private static Method loadMethod;
    private static Method saveMethod;
    private static Method setDistanceMethod;
    private static Method hasCitiesMethod;
    private static Method getCityDistanceMethod;

    private static WeakReference<World> netherWorldRef = new WeakReference<>(null);
    private static SoftReference<Object> softCityManager = new SoftReference<>(null);
    private static WeakReference<World> softWorldRef = new WeakReference<>(null);
    private static volatile long lastActiveTimeMs = 0L;

    private BetterNetherCityCacheHelper() {
    }

    private static boolean ensureHandles() {
        if (handlesResolved) {
            return handlesAvailable;
        }
        synchronized (LOCK) {
            if (handlesResolved) {
                return handlesAvailable;
            }
            try {
                Class<?> genClass = Class.forName("paulevs.betternether.world.BNWorldGenerator");
                cityManagerField = genClass.getDeclaredField("cityManager");
                cityManagerField.setAccessible(true);

                Class<?> csmClass = Class.forName("paulevs.betternether.structures.city.CityStructureManager");
                cityManagerCtor = csmClass.getConstructor(long.class);
                loadMethod = csmClass.getMethod("load", World.class);
                saveMethod = csmClass.getMethod("save", World.class);
                setDistanceMethod = csmClass.getMethod("setDistance", int.class);

                noiseField = csmClass.getDeclaredField("noise");
                noiseField.setAccessible(true);

                Class<?> cfgClass = Class.forName("paulevs.betternether.config.ConfigLoader");
                hasCitiesMethod = cfgClass.getMethod("hasCities");
                getCityDistanceMethod = cfgClass.getMethod("getCityDistance");

                handlesAvailable = true;
            } catch (Throwable t) {
                handlesAvailable = false;
                gctcore.LOGGER.warn("Failed to resolve BetterNether city manager reflection handles", t);
            }
            handlesResolved = true;
            return handlesAvailable;
        }
    }

    public static void onNetherWorldLoad(World world) {
        if (!ensureHandles()) {
            return;
        }
        synchronized (LOCK) {
            netherWorldRef = new WeakReference<>(world);
            softCityManager = new SoftReference<>(null);
            softWorldRef = new WeakReference<>(null);
            lastActiveTimeMs = 0L;
            try {
                cityManagerField.set(null, null);
                gctcore.LOGGER.info("GCT-Core deferred BetterNether CityStructureManager initialization until Nether generation");
            } catch (Throwable t) {
                gctcore.LOGGER.warn("Failed to reset BetterNether cityManager on Nether world load", t);
            }
        }
    }

    public static void ensureCityManagerForNether(World world) {
        if (!ensureHandles() || world == null || world.isRemote || world.provider == null
                || world.provider.getDimension() != -1) {
            return;
        }
        synchronized (LOCK) {
            try {
                if (!Boolean.TRUE.equals(hasCitiesMethod.invoke(null))) {
                    return;
                }
                lastActiveTimeMs = System.currentTimeMillis();

                Object existing = cityManagerField.get(null);
                if (existing != null) {
                    if (netherWorldRef.get() == world) {
                        return;
                    }
                    // Stale manager from a previous World instance: save only if its world still matches, then discard.
                    cityManagerField.set(null, null);
                }

                Object soft = softCityManager.get();
                if (soft != null && softWorldRef.get() == world) {
                    cityManagerField.set(null, soft);
                    netherWorldRef = new WeakReference<>(world);
                    softCityManager = new SoftReference<>(null);
                    gctcore.LOGGER.debug("Restored BetterNether CityStructureManager from soft cache");
                    return;
                }

                Object created = cityManagerCtor.newInstance(world.getSeed());
                loadMethod.invoke(created, world);
                int distance = ((Integer) getCityDistanceMethod.invoke(null)).intValue();
                setDistanceMethod.invoke(created, distance);

                cityManagerField.set(null, created);
                netherWorldRef = new WeakReference<>(world);
                softWorldRef = new WeakReference<>(world);
                softCityManager = new SoftReference<>(null);
                gctcore.LOGGER.info("GCT-Core lazily built BetterNether CityStructureManager on Nether generation");
            } catch (Throwable t) {
                gctcore.LOGGER.warn("Failed to lazily initialize BetterNether CityStructureManager", t);
            }
        }
    }

    /**
     * Computes the exact nearest BetterNether city chunk coordinate without allocating
     * {@code CityStructureManager} when it is currently unloaded.
     */
    public static BlockPos computeNearestCityIfUnloaded(World world, int chunkX, int chunkZ) {
        if (!ensureHandles() || world == null) {
            return null;
        }
        synchronized (LOCK) {
            try {
                if (cityManagerField.get(null) != null) {
                    return null;
                }
                int distance = Math.max(3, ((Integer) getCityDistanceMethod.invoke(null)).intValue());
                int variance = Math.max(1, distance / 3);
                int px = chunkX / distance;
                int pz = chunkZ / distance;
                long worldSeed = world.getSeed();
                long combinedSeed = px * RANDOM_A + pz * RANDOM_B + worldSeed + worldSeed;
                Random random = new Random(combinedSeed);
                int rx = px * distance + random.nextInt(variance) + variance;
                int rz = pz * distance + random.nextInt(variance) + variance;
                return new BlockPos(rx, 0, rz);
            } catch (Throwable t) {
                gctcore.LOGGER.warn("Failed to compute fallback nearest BetterNether city position", t);
                return null;
            }
        }
    }

    public static void onAfterWorldSave(World world) {
        if (!ensureHandles() || world == null || world.isRemote || world.provider == null
                || world.provider.getDimension() != -1) {
            return;
        }
        synchronized (LOCK) {
            if (world.playerEntities.isEmpty() && System.currentTimeMillis() - lastActiveTimeMs > IDLE_DEMOTE_MS) {
                demoteToSoftLocked(world);
            }
        }
    }

    public static void clearNoiseScratch(Object cityStructureManager) {
        if (!ensureHandles() || cityStructureManager == null) {
            return;
        }
        try {
            noiseField.set(cityStructureManager, null);
        } catch (Throwable ignored) {
        }
    }

    private static void saveAndDemoteLocked(World netherWorld) {
        if (!ensureHandles() || netherWorld == null) {
            return;
        }
        try {
            Object manager = cityManagerField.get(null);
            if (manager == null) {
                return;
            }
            safeSaveLocked(manager, netherWorld);
            demoteToSoftLocked(netherWorld);
        } catch (Throwable t) {
            gctcore.LOGGER.warn("Failed to save and demote BetterNether CityStructureManager", t);
        }
    }

    private static void demoteToSoftLocked(World netherWorld) {
        try {
            Object manager = cityManagerField.get(null);
            if (manager == null) {
                return;
            }
            softCityManager = new SoftReference<>(manager);
            softWorldRef = new WeakReference<>(netherWorld);
            cityManagerField.set(null, null);
            gctcore.LOGGER.info("GCT-Core released strong ref to BetterNether CityStructureManager (soft cache kept)");
        } catch (Throwable t) {
            gctcore.LOGGER.warn("Failed to demote BetterNether CityStructureManager to soft cache", t);
        }
    }

    private static void saveAndClearAllLocked(World worldForSave) {
        if (!ensureHandles()) {
            return;
        }
        try {
            Object manager = cityManagerField.get(null);
            if (manager != null && worldForSave != null) {
                safeSaveLocked(manager, worldForSave);
            }
            boolean hadRef = manager != null || softCityManager.get() != null;
            cityManagerField.set(null, null);
            softCityManager = new SoftReference<>(null);
            softWorldRef = new WeakReference<>(null);
            netherWorldRef = new WeakReference<>(null);
            if (hadRef) {
                gctcore.LOGGER.info("GCT-Core cleared BetterNether CityStructureManager on world unload");
            }
        } catch (Throwable t) {
            gctcore.LOGGER.warn("Failed to clear BetterNether CityStructureManager on unload", t);
        }
    }

    private static void safeSaveLocked(Object manager, World world) {
        try {
            ISaveHandler saveHandler = world.getSaveHandler();
            if (saveHandler == null) {
                return;
            }
            File worldDir = saveHandler.getWorldDirectory();
            if (worldDir != null && worldDir.exists()) {
                File dataDir = new File(worldDir, "data");
                if (dataDir.exists() || dataDir.mkdirs()) {
                    saveMethod.invoke(manager, world);
                }
            }
        } catch (Throwable t) {
            gctcore.LOGGER.warn("Failed to save BetterNether CityStructureManager state", t);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!Loader.isModLoaded("betternether") || !GCTMixinConfig.enableBetterNetherCityLazyCache) {
            return;
        }
        if (event.fromDim != -1 || event.player == null || event.player.world.isRemote) {
            return;
        }
        WorldServer nether = DimensionManager.getWorld(-1);
        if (nether == null) {
            return;
        }
        for (EntityPlayer p : nether.playerEntities) {
            if (p != event.player) {
                return;
            }
        }
        synchronized (LOCK) {
            saveAndDemoteLocked(nether);
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        if (!Loader.isModLoaded("betternether") || !GCTMixinConfig.enableBetterNetherCityLazyCache) {
            return;
        }
        World world = event.getWorld();
        if (world == null || world.isRemote || world.provider == null) {
            return;
        }
        int dim = world.provider.getDimension();
        if (dim == -1) {
            synchronized (LOCK) {
                saveAndClearAllLocked(world);
            }
        } else if (dim == 0) {
            synchronized (LOCK) {
                saveAndClearAllLocked(netherWorldRef.get());
            }
        }
    }
}
