package com.smd.gctcore.client;

import com.smd.gctcore.common.mixin.vanilla.renderer.AccessorBufferBuilder;
import com.smd.gctcore.gctcore;
import net.minecraft.client.renderer.BufferBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

/**
 * Releases native memory backing an off-heap {@link BufferBuilder} immediately once a temporary GUI closes,
 * avoiding waiting for a full GC / Cleaner cycle to free large direct buffers.
 */
public final class DirectBufferCleaner {

    private static final Object UNSAFE;
    private static final Method INVOKE_CLEANER_METHOD;
    private static final Method DIRECT_BUFFER_CLEANER_METHOD;
    private static final Method CLEANER_CLEAN_METHOD;
    private static boolean loggedFailure = false;

    static {
        Object unsafe = null;
        Method invokeCleaner = null;
        Method cleanerMethod = null;
        Method cleanMethod = null;

        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = theUnsafe.get(null);
            invokeCleaner = unsafeClass.getMethod("invokeCleaner", ByteBuffer.class);
        } catch (Throwable ignored) {
            // Fall through to Java 8 DirectBuffer.cleaner() lookup below.
        }

        if (invokeCleaner == null) {
            try {
                Class<?> directBufferClass = Class.forName("sun.nio.ch.DirectBuffer");
                cleanerMethod = directBufferClass.getMethod("cleaner");
                cleanMethod = cleanerMethod.getReturnType().getMethod("clean");
            } catch (Throwable ignored) {
                cleanerMethod = null;
                cleanMethod = null;
            }
        }

        UNSAFE = unsafe;
        INVOKE_CLEANER_METHOD = invokeCleaner;
        DIRECT_BUFFER_CLEANER_METHOD = cleanerMethod;
        CLEANER_CLEAN_METHOD = cleanMethod;
    }

    private DirectBufferCleaner() {
    }

    /**
     * Frees the direct {@link ByteBuffer} held inside {@code builder}, if present and direct.
     *
     * @param builder target buffer builder whose backing native memory is no longer needed
     */
    public static void freeBufferBuilder(BufferBuilder builder) {
        if (builder == null) {
            return;
        }
        ByteBuffer byteBuffer = ((AccessorBufferBuilder) builder).gctcore$getByteBuffer();
        freeDirectByteBuffer(byteBuffer);
    }

    /**
     * Frees a non-view direct {@link ByteBuffer} via {@code Unsafe.invokeCleaner} (Java 9+)
     * or {@code DirectBuffer.cleaner().clean()} (Java 8).
     */
    public static void freeDirectByteBuffer(ByteBuffer buffer) {
        if (buffer == null || !buffer.isDirect()) {
            return;
        }
        try {
            if (INVOKE_CLEANER_METHOD != null && UNSAFE != null) {
                INVOKE_CLEANER_METHOD.invoke(UNSAFE, buffer);
            } else if (DIRECT_BUFFER_CLEANER_METHOD != null && CLEANER_CLEAN_METHOD != null) {
                Object cleaner = DIRECT_BUFFER_CLEANER_METHOD.invoke(buffer);
                if (cleaner != null) {
                    CLEANER_CLEAN_METHOD.invoke(cleaner);
                }
            }
        } catch (Throwable t) {
            if (!loggedFailure) {
                loggedFailure = true;
                gctcore.LOGGER.warn("Failed to eagerly clean DirectByteBuffer; falling back to GC cleanup", t);
            }
        }
    }
}
