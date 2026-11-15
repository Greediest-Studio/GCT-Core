package com.smd.gctcore.common.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IFixType;
import net.minecraftforge.common.util.IDataFixerData;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/** 只在一次模板读取内复用已验证无变化的递归修复，旧版本和实际修复仍完整执行。 */
public final class TemplateFixScope {
    private static final ThreadLocal<TemplateFixScope> CURRENT = new ThreadLocal<>();
    private final Map<IDataFixer, IDataFixer> wrappers = new IdentityHashMap<>();

    private TemplateFixScope() {
    }

    public static NBTTagCompound process(DataFixer fixer, IFixType type, NBTTagCompound compound) {
        TemplateFixScope previous = CURRENT.get();
        CURRENT.set(new TemplateFixScope());
        try {
            return fixer.process(type, compound);
        } finally {
            if (previous == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(previous);
            }
        }
    }

    public static IDataFixer wrap(IDataFixer fixer) {
        TemplateFixScope scope = CURRENT.get();
        if (scope == null || !(fixer instanceof IDataFixerData) || fixer instanceof MemoizingFixer) {
            return fixer;
        }
        return scope.wrappers.computeIfAbsent(fixer, key -> new MemoizingFixer((IDataFixerData) key));
    }

    private static final class MemoizingFixer implements IDataFixerData {
        private final IDataFixerData delegate;
        private final Map<NBTTagCompound, Map<IFixType, Entry>> unchanged = new IdentityHashMap<>();

        private MemoizingFixer(IDataFixerData delegate) {
            this.delegate = delegate;
        }

        @Override
        public int getVersion(String modid) {
            return delegate.getVersion(modid);
        }

        @Override
        public NBTTagCompound process(IFixType type, NBTTagCompound compound, int version) {
            Map<IFixType, Entry> entries = unchanged.get(compound);
            Entry entry = entries == null ? null : entries.get(type);
            if (entry != null && entry.version == version && entry.snapshot.equals(compound)) {
                return compound;
            }

            NBTTagCompound before = compound.copy();
            NBTTagCompound result = delegate.process(type, compound, version);
            // 仅缓存真正无变化的调用；其他修复器改过字段后，完整 NBT 比较会使缓存失效。
            if (before.equals(result)) {
                unchanged.computeIfAbsent(compound, key -> new HashMap<>())
                        .put(type, new Entry(version, before));
            }
            return result;
        }
    }

    private static final class Entry {
        private final int version;
        private final NBTTagCompound snapshot;

        private Entry(int version, NBTTagCompound snapshot) {
            this.version = version;
            this.snapshot = snapshot;
        }
    }
}
