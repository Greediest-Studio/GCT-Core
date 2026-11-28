package com.smd.gctcore.common.mixin.randomcomplement;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.me.ItemRepo;
import com.circulation.random_complement.client.RCGuiMEMonitorable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

// RC 1.9.6 的同位置 Redirect 使用默认优先级 1000，由这里接管两轮排序。
@Mixin(value = ItemRepo.class, remap = false, priority = 1100)
public abstract class MixinItemRepo {

    @Redirect(method = "updateView", at = @At(value = "INVOKE",
            target = "Ljava/util/List;sort(Ljava/util/Comparator;)V"), require = 1)
    private void gctcore$sortWithCachedPriority(List<IAEItemStack> view, Comparator<IAEItemStack> comparator) {
        view.sort(comparator);
        if (view.size() < 2) {
            return;
        }

        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (!(screen instanceof RCGuiMEMonitorable)) {
            return;
        }

        // 空集合也是有效结果；只在本轮复用，下轮仍读取 RC 的最新缓存。
        Set<IAEItemStack> priorityItems = ((RCGuiMEMonitorable) screen).r$getCpuCache();
        if (priorityItems.isEmpty()) {
            return;
        }

        // List.sort 是稳定排序，同组物品保持 AE 的名称、数量等排序结果。
        view.sort((a, b) -> Boolean.compare(priorityItems.contains(b), priorityItems.contains(a)));
    }
}
