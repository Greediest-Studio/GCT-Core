package github.kasuminova.stellarcore.client.pool;

import github.kasuminova.stellarcore.common.pool.AsyncCanonicalizePool;
import github.kasuminova.stellarcore.common.pool.DeferredCanonicalizable;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import java.util.function.Consumer;

// Compile-time stub only - excluded from packaged jar by build.gradle
public class StellarUnpackedDataPool {

    public static void canonicalizeAsync(float[][][] unpackedData, Consumer<float[][][]> callback) {}

    public static void canonicalizeDeferred(DeferredCanonicalizable<float[][][]> target) {}

    public static float[][][] canonicalize(float[][][] unpackedData) {
        return unpackedData;
    }

    public static void canonicalizeAsync(int[] vertexData, Consumer<int[]> callback) {}

    public static int[] canonicalize(int[] vertexData) {
        return vertexData;
    }

    public static void reset() {}

    public static abstract class UnpackedDataPool<T> extends AsyncCanonicalizePool<T> {
        private ObjectOpenCustomHashSet<T> pool;

        protected abstract Hash.Strategy<T> getStrategy();

        @Override
        protected T canonicalizeInternal(T value) {
            return value;
        }

        @Override
        protected ObjectSet<T> getPoolKeySet() {
            return pool;
        }

        @Override
        protected void clearPool() {}
    }

    public static class UnpackedDataPoolLevel1 extends UnpackedDataPool<float[][][]> {
        @Override
        protected Hash.Strategy<float[][][]> getStrategy() {
            return null;
        }

        @Override
        protected String getName() {
            return "UnpackedDataPool-L1";
        }
    }

    public static class VertexDataPool extends AsyncCanonicalizePool<int[]> {
        private ObjectOpenCustomHashSet<int[]> pool;

        @Override
        protected int[] canonicalizeInternal(int[] value) {
            return value;
        }

        @Override
        protected String getName() {
            return "VertexDataPool";
        }

        @Override
        protected ObjectSet<int[]> getPoolKeySet() {
            return pool;
        }

        @Override
        protected void clearPool() {}
    }
}
