package com.smd.gctcore.common.mixin.vanilla.client;

import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Field;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    private static final String[] SHADERS_CLASS_NAMES = {
            "shadersmod.client.Shaders",
            "net.optifine.shaders.Shaders"
    };

    private static final String[] SHADER_PACK_NAME_FIELDS = {
            "currentShaderName",
            "shaderPackName",
            "currentshadername",
            "configShaderPack"
    };

    private static Field gctcore$shaderPackLoadedField;
    private static Field gctcore$shaderPackField;
    private static Field gctcore$shaderPackNameField;
    private static Class<?> gctcore$resolvedShadersClass;
    private static boolean gctcore$shaderFieldsResolved;

    @Redirect(
            method = "setupFog(IF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/state/IBlockState;getMaterial()Lnet/minecraft/block/material/Material;",
                    ordinal = 1
            ),
            require = 0
    )
    private Material gctcore$useNormalFogRangeInLava(IBlockState state) {
        Material material = state.getMaterial();
        return material == Material.LAVA && !gctcore$isShaderPackActive() ? Material.AIR : material;
    }

    private static boolean gctcore$isShaderPackActive() {
        for (String className : SHADERS_CLASS_NAMES) {
            try {
                Class<?> shadersClass = Class.forName(className, false, MixinEntityRenderer.class.getClassLoader());
                gctcore$resolveShaderFields(shadersClass);

                if (gctcore$shaderPackLoadedField != null && !gctcore$shaderPackLoadedField.getBoolean(null)) {
                    return false;
                }

                if (gctcore$shaderPackNameField != null) {
                    Object name = gctcore$shaderPackNameField.get(null);
                    if (gctcore$isRealShaderPackName(name)) {
                        return true;
                    }
                }

                if (gctcore$shaderPackField != null) {
                    Object shaderPack = gctcore$shaderPackField.get(null);
                    return shaderPack != null && gctcore$isRealShaderPackName(shaderPack.toString());
                }
            } catch (ClassNotFoundException ignored) {
                gctcore$shaderFieldsResolved = false;
            } catch (Throwable ignored) {
                return false;
            }
        }

        return false;
    }

    private static void gctcore$resolveShaderFields(Class<?> shadersClass) {
        if (gctcore$shaderFieldsResolved && gctcore$resolvedShadersClass == shadersClass) {
            return;
        }

        gctcore$resolvedShadersClass = shadersClass;
        gctcore$shaderPackLoadedField = gctcore$findField(shadersClass, "shaderPackLoaded");
        gctcore$shaderPackField = gctcore$findField(shadersClass, "shaderPack");

        for (String fieldName : SHADER_PACK_NAME_FIELDS) {
            gctcore$shaderPackNameField = gctcore$findField(shadersClass, fieldName);
            if (gctcore$shaderPackNameField != null) {
                break;
            }
        }

        gctcore$shaderFieldsResolved = true;
    }

    private static Field gctcore$findField(Class<?> owner, String name) {
        try {
            Field field = owner.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException ignored) {
            return null;
        }
    }

    private static boolean gctcore$isRealShaderPackName(Object value) {
        if (!(value instanceof String)) {
            return false;
        }

        String name = ((String) value).trim();
        return !name.isEmpty()
                && !"off".equalsIgnoreCase(name)
                && !"none".equalsIgnoreCase(name)
                && !"null".equalsIgnoreCase(name)
                && !"internal".equalsIgnoreCase(name)
                && !"(internal)".equalsIgnoreCase(name);
    }
}
