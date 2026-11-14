package github.kasuminova.stellarcore.common.pool;

import java.util.Set;

// Compile-time stub only - excluded from packaged jar by build.gradle
public abstract class AsyncCanonicalizePool<T> extends AsyncCanonicalizePoolBase<T> {

    @Override
    public final T canonicalize(T value) {
        return null;
    }

    protected abstract T canonicalizeInternal(T value);

    @Override
    public synchronized void clear() {}

    @Override
    public long getProcessedCount() {
        return 0L;
    }

    @Override
    public int getUniqueCount() {
        return 0;
    }

    protected abstract void clearPool();

    protected abstract Set<T> getPoolKeySet();
}
