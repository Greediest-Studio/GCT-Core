package com.smd.gctcore.common.mixin.additions;

import com.smd.gctcore.common.util.AdvancementResourceScope;
import net.minecraft.advancements.Advancement;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Pseudo
@Mixin(targets = "com.tmtravlr.additions.addon.advancements.ExtendedAdvancementManager", remap = false)
public abstract class MixinAdvancementResources {
    @Redirect(method = "func_192779_a", at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/common/ForgeHooks;loadAdvancements(Ljava/util/Map;)Z"), require = 1)
    private boolean gctcore$loadAdvancements(Map<ResourceLocation, Advancement.Builder> advancements) {
        return AdvancementResourceScope.load(advancements);
    }
}
