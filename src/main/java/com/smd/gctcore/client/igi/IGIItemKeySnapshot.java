package com.smd.gctcore.client.igi;

public interface IGIItemKeySnapshot {

    /** 查找键写入长期缓存前，将借用的 NBT 转为独立快照。 */
    void gctcore$freezeNbtSnapshot();
}
