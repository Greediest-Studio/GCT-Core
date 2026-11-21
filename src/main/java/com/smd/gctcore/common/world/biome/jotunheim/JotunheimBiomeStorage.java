package com.smd.gctcore.common.world.biome.jotunheim;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Jotunheim's chunk biome persistence boundary. REID deliberately disables the
 * vanilla byte-array setter; its public BiomeApi must receive full integer IDs.
 * Optional APIs are resolved lazily after chunk creation, never during mod loading.
 */
public final class JotunheimBiomeStorage {
    private static final int COLUMN_COUNT = 256;

    private JotunheimBiomeStorage() {}

    public static void write(Chunk chunk, Biome[] biomes) {
        if (biomes == null || biomes.length != COLUMN_COUNT) {
            throw new IllegalArgumentException("Jotunheim chunks require exactly 256 biomes");
        }
        int[] ids = new int[COLUMN_COUNT];
        for (int i = 0; i < ids.length; i++) {
            if (biomes[i] == null) throw new IllegalStateException("Missing Jotunheim biome at column " + i);
            ids[i] = Biome.getIdForBiome(biomes[i]);
            if (ids[i] < 0) throw new IllegalStateException("Unregistered Jotunheim biome: " + biomes[i].getRegistryName());
        }
        writeIds(chunk, ids);
    }

    /** Returns a copy, including uninitialized sentinels; it does not query the provider. */
    static int[] readIds(Chunk chunk) {
        int[] result = AccessHolder.BACKEND.read(chunk);
        if (result == null || result.length != COLUMN_COUNT) {
            throw new IllegalStateException("Invalid Jotunheim chunk biome storage length at " + chunk.x + "," + chunk.z);
        }
        return result.clone();
    }

    static void writeIds(Chunk chunk, int[] ids) {
        if (ids == null || ids.length != COLUMN_COUNT) throw new IllegalArgumentException("Expected 256 chunk biome IDs");
        for (int id : ids) {
            if (id < 0 || Biome.getBiome(id) == null) throw new IllegalStateException("Cannot persist unknown Jotunheim biome ID " + id);
        }
        AccessHolder.BACKEND.write(chunk, ids);
        chunk.markDirty();
    }

    private interface Backend {
        int[] read(Chunk chunk);
        void write(Chunk chunk, int[] ids);
    }

    private static final class AccessHolder {
        static final Backend BACKEND = discover();
    }

    private static Backend discover() {
        ClassLoader loader = JotunheimBiomeStorage.class.getClassLoader();
        try {
            // REID 2.x public API, with no hard optional-mod class references.
            Class<?> apiType = Class.forName("tff.reid.api.BiomeApi", true, loader);
            Class<?> accessorType = Class.forName("tff.reid.api.biome.BiomeAccessor", false, loader);
            final Object api = apiType.getField("INSTANCE").get(null);
            final Method readAccessor = apiType.getMethod("getBiomeAccessor", Chunk.class);
            final Method readBiomes = accessorType.getMethod("getBiomes");
            final Method replace = apiType.getMethod("replaceBiomes", Chunk.class, int[].class);
            return new Backend() {
                @Override public int[] read(Chunk chunk) {
                    return (int[]) invoke(readBiomes, invoke(readAccessor, api, chunk));
                }
                @Override public void write(Chunk chunk, int[] ids) { invoke(replace, api, chunk, ids); }
            };
        } catch (ClassNotFoundException absent) {
            // Older JEID exposes integer storage directly on its transformed Chunk.
            try {
                final Method read = Chunk.class.getMethod("getIntBiomeArray");
                final Method write = Chunk.class.getMethod("setIntBiomeArray", int[].class);
                return new Backend() {
                    @Override public int[] read(Chunk chunk) { return (int[]) invoke(read, chunk); }
                    @Override public void write(Chunk chunk, int[] ids) { invoke(write, chunk, ids); }
                };
            } catch (NoSuchMethodException vanilla) {
                return new Backend() {
                    @Override public int[] read(Chunk chunk) {
                        byte[] bytes = chunk.getBiomeArray();
                        int[] result = new int[bytes.length];
                        for (int i = 0; i < bytes.length; i++) result[i] = bytes[i] & 255;
                        return result;
                    }
                    @Override public void write(Chunk chunk, int[] ids) {
                        byte[] bytes = new byte[ids.length];
                        for (int i = 0; i < ids.length; i++) {
                            // 255 is vanilla's uninitialized sentinel, not a usable biome slot.
                            if (ids[i] >= 255) throw new IllegalStateException("Jotunheim biome ID " + ids[i]
                                    + " requires an extended biome-ID mod; refusing to truncate it to a byte");
                            bytes[i] = (byte) ids[i];
                        }
                        chunk.setBiomeArray(bytes);
                    }
                };
            }
        } catch (ReflectiveOperationException brokenApi) {
            throw new IllegalStateException("Cannot access the installed REID biome API for Jotunheim", brokenApi);
        }
    }

    private static Object invoke(Method method, Object receiver, Object... arguments) {
        try {
            return method.invoke(receiver, arguments);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot access Jotunheim biome storage through " + method, e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Jotunheim biome storage API failed in " + method, e.getCause());
        }
    }
}
