package github.kasuminova.stellarcore.client.pool;

import github.kasuminova.stellarcore.common.pool.AsyncCanonicalizePool;
import net.minecraft.client.renderer.block.model.BakedQuad;

import java.util.Set;

// Compile-time stub only - excluded from packaged jar by build.gradle
public class BakedQuadPool extends AsyncCanonicalizePool<BakedQuad> {

    public static final BakedQuadPool INSTANCE = new BakedQuadPool();

    public void canonicalizeAsync(Runnable task) {}

    @Override
    protected BakedQuad canonicalizeInternal(BakedQuad quad) {
        return quad;
    }

    @Override
    protected void clearPool() {}

    @Override
    protected Set<BakedQuad> getPoolKeySet() {
        return null;
    }

    @Override
    protected String getName() {
        return "BakedQuadPool";
    }
}
