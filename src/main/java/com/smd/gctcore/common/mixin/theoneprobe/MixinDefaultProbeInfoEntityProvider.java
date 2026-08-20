package com.smd.gctcore.common.mixin.theoneprobe;

import net.minecraft.entity.Entity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import mcjty.theoneprobe.apiimpl.providers.DefaultProbeInfoEntityProvider;

/** Keeps Botaniverse's four shared spark entity IDs localized in TOPCE. */
@Mixin(value = DefaultProbeInfoEntityProvider.class, remap = false)
public abstract class MixinDefaultProbeInfoEntityProvider {

    @Inject(
            // TOPCE 1.3.x returns ITextComponent here; newer TOPCE builds
            // changed the declared return type.  Name-only matching keeps the
            // compatibility mixin usable with both variants.
            method = "getName",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 0
    )
    private static void gctcore$localizeBotaniverseSparkName(
            Entity entity,
            CallbackInfoReturnable<ITextComponent> cir
    ) {
        String translationKey = getBotaniverseSparkTranslationKey(entity);
        if (translationKey != null) {
            // Preserve the translation component so TOPCE resolves the name
            // using the probing player's language on the client.
            cir.setReturnValue(new TextComponentTranslation(translationKey));
        }
    }

    private static String getBotaniverseSparkTranslationKey(Entity entity) {
        String className = entity.getClass().getName();
        if (className.equals("com.aeternal.botaniverse.common.entity.sparks.EntitySparkNilfheim")) {
            return "entity.botaniverse:spark_nilfheim.name";
        }
        if (className.equals("com.aeternal.botaniverse.common.entity.sparks.EntitySparkMuspelheim")) {
            return "entity.botaniverse:spark_muspelheim.name";
        }
        if (className.equals("com.aeternal.botaniverse.common.entity.sparks.EntitySparkAlfheim")) {
            return "entity.botaniverse:spark_alfheim.name";
        }
        if (className.equals("com.aeternal.botaniverse.common.entity.sparks.EntitySparkAsgard")) {
            return "entity.botaniverse:spark_asgard.name";
        }
        return null;
    }
}
