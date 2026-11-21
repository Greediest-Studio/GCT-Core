package com.smd.gctcore.common.integration.lostcities;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ObjectIntIdentityMap;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Process-lifetime bidirectional local palette for Lost Cities block state tokens.
 *
 * <p>Lost Cities encodes {@link IBlockState} instances into 16-bit {@code char} tokens
 * ({@code 0..65535}) via {@link Block#BLOCK_STATE_IDS}. Under REID/JEID, block state IDs
 * exceed 65535 and truncate when cast with {@code (char)}, corrupting structures.
 * Mapping each distinct {@link IBlockState} used by Lost Cities to a compact token in
 * {@code 0..65534} keeps 16-bit storage lossless while preserving {@code 0} for air and
 * {@code -1} ({@code 0xFFFF}) for unmapped/null states.</p>
 */
public final class LostCitiesStatePalette {

    private static final int MAX_VALID_ID = 0xFFFF;
    private static final int INITIAL_CAPACITY = 512;

    private static final Object LOCK = new Object();
    private static final ConcurrentHashMap<IBlockState, Integer> STATE_TO_ID = new ConcurrentHashMap<>();
    private static volatile IBlockState[] statesById = new IBlockState[INITIAL_CAPACITY];
    private static int nextId = 1;

    private LostCitiesStatePalette() {
    }

    @SuppressWarnings("unchecked")
    public static int getStateId(ObjectIntIdentityMap<?> map, Object value) {
        if (map == Block.BLOCK_STATE_IDS) {
            return encode((IBlockState) value);
        }
        return ((ObjectIntIdentityMap<Object>) map).get(value);
    }

    public static Object getStateById(ObjectIntIdentityMap<?> map, int id) {
        if (map == Block.BLOCK_STATE_IDS) {
            return decode(id);
        }
        return map.getByValue(id);
    }

    public static int encode(IBlockState state) {
        if (state == null) {
            return -1;
        }
        if (state == Blocks.AIR.getDefaultState()) {
            return 0;
        }
        Integer existing = STATE_TO_ID.get(state);
        if (existing != null) {
            return existing.intValue();
        }
        synchronized (LOCK) {
            existing = STATE_TO_ID.get(state);
            if (existing != null) {
                return existing.intValue();
            }
            int id = nextId;
            if (id >= MAX_VALID_ID) {
                throw new IllegalStateException("Lost Cities local block state palette exceeded 65534 unique states");
            }
            IBlockState[] arr = statesById;
            if (id >= arr.length) {
                int newCapacity = Math.min(MAX_VALID_ID, arr.length << 1);
                arr = Arrays.copyOf(arr, newCapacity);
            }
            arr[id] = state;
            // Volatile store publishes arr[id] to lock-free decode() readers.
            statesById = arr;
            nextId = id + 1;
            STATE_TO_ID.put(state, id);
            return id;
        }
    }

    public static IBlockState decode(int id) {
        if (id == 0) {
            return Blocks.AIR.getDefaultState();
        }
        IBlockState[] arr = statesById;
        return (id > 0 && id < arr.length) ? arr[id] : null;
    }
}
