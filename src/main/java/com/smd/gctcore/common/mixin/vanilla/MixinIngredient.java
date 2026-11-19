package com.smd.gctcore.common.mixin.vanilla;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

/**
 * Serializes access to Ingredient's WeakHashMap-backed instance set.
 * Forge invalidates this set during registry remapping while recipe loaders
 * can still construct ingredients on another thread.
 */
@Mixin(Ingredient.class)
public abstract class MixinIngredient {

    @Shadow(remap = false)
    @Final
    private static Set<Ingredient> INSTANCES;

    @Redirect(
            method = "<init>([Lnet/minecraft/item/ItemStack;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"),
            require = 1
    )
    private static boolean gctcore$lockInstanceRegistration(Set<Object> instances, Object ingredient) {
        synchronized (instances) {
            return instances.add(ingredient);
        }
    }

    @Inject(method = "invalidateAll", at = @At("HEAD"), cancellable = true, remap = false, require = 1)
    private static void gctcore$invalidateAllUnderLock(CallbackInfo ci) {
        synchronized (INSTANCES) {
            for (Ingredient ingredient : INSTANCES) {
                if (ingredient != null) {
                    ((IngredientInvoker) (Object) ingredient).gctcore$invalidate();
                }
            }
        }
        ci.cancel();
    }
}
