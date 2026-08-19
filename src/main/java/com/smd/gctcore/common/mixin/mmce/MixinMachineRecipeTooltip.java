package com.smd.gctcore.common.mixin.mmce;

import hellfirepvp.modularmachinery.common.crafting.MachineRecipe;
import net.minecraft.client.resources.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds the small piece of formatting that MMCE's RecipePrimer API lacks:
 * {@code key|argument} is formatted as {@code I18n.format(key, argument)}.
 * Ordinary MMCE/CraftTweaker tooltip strings follow the original path
 * unchanged, while migrated altar recipes can use one reusable key instead
 * of one language entry for every LP value.
 */
@Mixin(value = MachineRecipe.class, remap = false)
public abstract class MixinMachineRecipeTooltip {

    @Inject(method = "getFormattedTooltip", at = @At("HEAD"), cancellable = true)
    @SuppressWarnings("unchecked")
    private void gctcore$formatParameterizedTooltip(
            final CallbackInfoReturnable<List<String>> callback) {
        final MachineRecipe recipe = (MachineRecipe) (Object) this;
        final List<String> source = recipe.getTooltipList();
        boolean hasParameterizedEntry = false;
        for (String tooltip : source) {
            final int separator = tooltip.indexOf('|');
            if (separator > 0 && I18n.hasKey(tooltip.substring(0, separator))) {
                hasParameterizedEntry = true;
                break;
            }
        }
        if (!hasParameterizedEntry) {
            return;
        }

        final List<String> formatted = new ArrayList<>(source.size());
        for (String tooltip : source) {
            final int separator = tooltip.indexOf('|');
            if (separator > 0) {
                final String key = tooltip.substring(0, separator);
                if (I18n.hasKey(key)) {
                    formatted.add(I18n.format(key, tooltip.substring(separator + 1)));
                    continue;
                }
            }
            formatted.add(I18n.hasKey(tooltip) ? I18n.format(tooltip) : tooltip);
        }
        callback.setReturnValue(formatted);
    }
}
