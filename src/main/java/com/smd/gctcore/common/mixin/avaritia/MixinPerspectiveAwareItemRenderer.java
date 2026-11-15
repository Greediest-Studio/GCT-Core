package com.smd.gctcore.common.mixin.avaritia;

import com.smd.gctcore.client.AvaritiaWorldReferenceCleanup;
import morph.avaritia.client.render.item.PerspectiveAwareItemRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PerspectiveAwareItemRenderer.class, remap = false)
public abstract class MixinPerspectiveAwareItemRenderer implements AvaritiaWorldReferenceCleanup.WorldReference {

    @Shadow protected EntityLivingBase renderEntity;
    @Shadow protected World world;
    @Shadow protected BlockPos entityPos;

    @Inject(method = "<init>", at = @At("RETURN"), require = 1)
    private void gctcore$trackWorldReferences(CallbackInfo ci) {
        AvaritiaWorldReferenceCleanup.register(this);
    }

    @Override
    public void gctcore$clearWorldReferences(World unloadedWorld) {
        if (world == unloadedWorld || (renderEntity != null && renderEntity.world == unloadedWorld)) {
            renderEntity = null;
            world = null;
            entityPos = null;
        }
    }
}
