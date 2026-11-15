package com.smd.gctcore.common.mixin.projecte;

import com.smd.gctcore.common.util.KnowledgeStackIndex;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Pseudo
@Mixin(targets = "moze_intel.projecte.playerData.Transmutation", remap = false)
public abstract class MixinTransmutationKnowledge {
    @Unique
    private static final ThreadLocal<KnowledgeStackIndex> gctcore$knowledgeIndex = new ThreadLocal<>();

    @Inject(method = "cacheFullKnowledge", at = @At("HEAD"), require = 1)
    private static void gctcore$beginKnowledge(CallbackInfo ci) {
        gctcore$knowledgeIndex.set(new KnowledgeStackIndex());
    }

    @Inject(method = "cacheFullKnowledge", at = @At("RETURN"), require = 1)
    private static void gctcore$endKnowledge(CallbackInfo ci) {
        gctcore$knowledgeIndex.remove();
    }

    @Redirect(method = "cacheFullKnowledge", at = @At(value = "INVOKE",
            target = "Lmoze_intel/projecte/utils/ItemHelper;containsItemStack(Ljava/util/List;Lnet/minecraft/item/ItemStack;)Z"), require = 1)
    private static boolean gctcore$containsKnowledge(List<ItemStack> knowledge, ItemStack candidate) {
        return gctcore$knowledgeIndex.get().contains(candidate);
    }

    @Redirect(method = "cacheFullKnowledge", at = @At(value = "INVOKE",
            target = "Ljava/util/List;add(Ljava/lang/Object;)Z"), require = 1)
    private static boolean gctcore$addKnowledge(List<ItemStack> knowledge, Object value) {
        ItemStack stack = (ItemStack) value;
        boolean added = knowledge.add(stack);
        if (added) {
            gctcore$knowledgeIndex.get().add(stack);
        }
        return added;
    }
}
