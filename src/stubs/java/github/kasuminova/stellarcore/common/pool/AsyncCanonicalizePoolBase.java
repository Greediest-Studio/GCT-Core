package github.kasuminova.stellarcore.common.pool;

import java.util.function.Consumer;

// Compile-time stub only - excluded from packaged jar by build.gradle
public abstract class AsyncCanonicalizePoolBase<T> {

    public void canonicalizeAsync(T value, Consumer<T> callback) {}

    public void canonicalizeDeferred(DeferredCanonicalizable<T> value) {}

    public abstract T canonicalize(T value);

    public abstract long getProcessedCount();

    public abstract int getUniqueCount();

    public abstract void clear();

    protected abstract String getName();
}
