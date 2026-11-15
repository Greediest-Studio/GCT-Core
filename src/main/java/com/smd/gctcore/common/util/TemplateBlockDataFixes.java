package com.smd.gctcore.common.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IFixType;

/** 仅用于结构模板；已是 1.12.2 的原版结构方块无需遍历模组数据修复器。 */
public final class TemplateBlockDataFixes {
    private TemplateBlockDataFixes() {
    }

    public static NBTTagCompound process(IDataFixer fixer, IFixType type,
                                        NBTTagCompound compound, int version) {
        if (version >= 1343 && type == FixTypes.BLOCK_ENTITY
                && "minecraft:structure_block".equals(compound.getString("id"))
                && !compound.hasKey("ForgeCaps") && !compound.hasKey("Items")) {
            // 保留完整 NBT 和 DATA 标记；空标记仍按原逻辑清除对应位置的方块。
            return compound;
        }
        return fixer.process(type, compound, version);
    }
}
