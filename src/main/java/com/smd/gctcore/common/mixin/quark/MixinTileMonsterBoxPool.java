package com.smd.gctcore.common.mixin.quark;

import com.smd.gctcore.gctcore;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quark parses every monster box spawn pool entry through {@code ForgeRegistries.ENTITIES}
 * and stores the result as a map key, without checking for null. An id that belongs to a
 * mod which is not installed resolves to null, and the pool then contains a null key.
 *
 * <p>Depending on the Quark build, {@code TileMonsterBox.spawnMobs()} either calls
 * {@code EntityEntry.newInstance} on that null key or skips it and dereferences the
 * still-null entity afterwards; both paths throw a NullPointerException and crash the
 * server tick when the box releases its mobs.</p>
 *
 * <p>This drops entries that failed to resolve and renormalises the remaining weights so
 * the pool still sums to one and a mob is always selected. Duplicate ids are merged
 * instead of overwriting each other's weight.</p>
 */
@Pseudo
@Mixin(targets = "vazkii.quark.world.tile.TileMonsterBox$Pool", remap = false)
public class MixinTileMonsterBoxPool {

    @Unique
    private static final Set<String> REPORTED = ConcurrentHashMap.newKeySet();
    @Unique
    private static final ResourceLocation ZOMBIE_ID = new ResourceLocation("minecraft", "zombie");

    @Shadow(remap = false)
    public Map<EntityEntry, Double> entities;

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/registries/IForgeRegistry;getValue(Lnet/minecraft/util/ResourceLocation;)Lnet/minecraftforge/registries/IForgeRegistryEntry;"
            ),
            require = 1
    )
    private IForgeRegistryEntry<?> gctcore$resolveSpawnPoolEntity(IForgeRegistry<EntityEntry> registry, ResourceLocation id) {
        IForgeRegistryEntry<?> entry = registry.getValue(id);
        if (entry == null && REPORTED.add(id.toString())) {
            gctcore.LOGGER.warn(
                    "[Quark] Monster box spawn pool references unknown entity '{}' (its mod is not installed); "
                            + "the entry is ignored. Remove it from the Quark 'Pools' config to silence this.",
                    id);
        }
        return entry;
    }

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
            ),
            require = 1
    )
    private Object gctcore$mergeSpawnPoolWeight(Map<Object, Object> entities, Object key, Object value) {
        if (key == null) {
            // Unresolved entity: drop it here instead of poisoning the pool with a null key.
            return null;
        }

        Object existing = entities.get(key);
        if (existing instanceof Double && value instanceof Double) {
            entities.put(key, (Double) existing + (Double) value);
            return existing;
        }
        return entities.put(key, value);
    }

    /**
     * Dropping unresolved entries leaves the pool summing to less than one, and a config
     * whose weights never add up to one has the same effect: the selection loop in
     * spawnMobs() can finish without picking a mob. Normalise the weights so a mob is
     * always selected.
     */
    @Inject(method = "<init>", at = @At("TAIL"), require = 1)
    private void gctcore$normaliseSpawnPoolWeights(String source, CallbackInfo ci) {
        if (entities.isEmpty()) {
            EntityEntry zombie = ForgeRegistries.ENTITIES.getValue(ZOMBIE_ID);
            if (zombie != null) {
                entities.put(zombie, 1.0D);
            }
            return;
        }

        double total = 0.0D;
        for (Double weight : entities.values()) {
            if (weight != null && weight > 0.0D) {
                total += weight;
            }
        }
        if (total <= 0.0D) {
            EntityEntry zombie = ForgeRegistries.ENTITIES.getValue(ZOMBIE_ID);
            entities.clear();
            if (zombie != null) {
                entities.put(zombie, 1.0D);
            }
            return;
        }
        if (total >= 1.0D) {
            // A pool that already covers the whole range keeps its configured weights.
            return;
        }

        // Dropping unresolved entries (or a config that never sums to 1) can leave a gap
        // that no random roll lands in, which would leave the spawned entity null.
        for (Map.Entry<EntityEntry, Double> entry : entities.entrySet()) {
            Double weight = entry.getValue();
            entry.setValue(weight == null ? 0.0D : weight / total);
        }
    }
}
