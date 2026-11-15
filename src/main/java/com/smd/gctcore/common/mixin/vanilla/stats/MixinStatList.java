package com.smd.gctcore.common.mixin.vanilla.stats;

import com.smd.gctcore.common.stats.LazyStatNameHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Skips eager {@link ItemStack#getTextComponent()} allocation (including {@code NBTTagCompound}, SNBT,
 * {@code HoverEvent}, {@code Style}, and {@code TextComponentString} trees) during {@link StatList}
 * initialization while keeping {@link StatList} locals and {@code StatCrafting#registerStat()} calls intact
 * for full compatibility with RoughlyEnoughIDs.
 */
@Mixin(StatList.class)
public abstract class MixinStatList {

    @Redirect(
            method = {
                    "initCraftableStats",
                    "initMiningStats",
                    "initStats",
                    "initItemDepleteStats",
                    "initPickedUpAndDroppedStats"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;getTextComponent()Lnet/minecraft/util/text/ITextComponent;"
            ),
            require = 1
    )
    private static ITextComponent gctcore$deferItemStackTextComponent(ItemStack stack) {
        return LazyStatNameHelper.LAZY_STAT_SENTINEL;
    }
}
