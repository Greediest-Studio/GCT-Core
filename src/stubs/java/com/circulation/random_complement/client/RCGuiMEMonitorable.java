package com.circulation.random_complement.client;

import appeng.api.storage.data.IAEItemStack;

import java.util.Set;

// 仅用于编译；运行时使用 RandomComplement 提供的接口，不打入 GCT-Core。
public interface RCGuiMEMonitorable {
    Set<IAEItemStack> r$getCpuCache();
}
