package com.smd.gctcore.common.mixin.fancymenu;

import de.keksuccino.konkrete.resources.WebTextureResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 修复 FancyMenu 在构建主菜单玩家模型时，主线程同步请求网络皮肤导致进程假死（无响应/卡在100%）的问题。
 * 1. 将直接配置的 skinUrl / capeUrl 改为后台异步拉取；
 * 2. 在元素构造完成后，立即为 entity 预置默认史蒂夫/亚历克斯皮肤，并标记 checked，
 *    彻底阻断 FancyMenu 渲染线程内部 getSkin() / getCape() 触发的同步主线程阻塞请求；
 * 3. 启动后台守护线程拉取在线玩家皮肤，拉取完成后在 GL 主线程异步更新模型纹理。
 */
@Pseudo
@Mixin(targets = "de.keksuccino.fancymenu.menu.fancy.item.playerentity.PlayerEntityCustomizationItem", remap = false)
public abstract class MixinPlayerEntityCustomizationItem {

    /**
     * 拦截构造器中对 skinUrl 的同步请求（ordinal = 0）
     */
    @Redirect(
        method = "<init>(Lde/keksuccino/konkrete/properties/PropertiesSection;)V",
        at = @At(
            value = "INVOKE",
            target = "Lde/keksuccino/konkrete/resources/TextureHandler;getWebResource(Ljava/lang/String;)Lde/keksuccino/konkrete/resources/WebTextureResourceLocation;",
            ordinal = 0
        ),
        remap = false
    )
    private WebTextureResourceLocation gct$asyncLoadSkin(String skinUrl) {
        if (skinUrl == null || skinUrl.trim().isEmpty()) {
            return null;
        }

        Object ent = getFieldValue(this, "entity");
        if (ent != null) {
            setFieldValue(ent, "skinLocation", DefaultPlayerSkin.getDefaultSkinLegacy());
            setFieldValue(ent, "skinChecked", true);
        }

        final String targetUrl = skinUrl;
        Thread thread = new Thread(() -> {
            BufferedImage img = downloadImage(targetUrl);
            if (img != null) {
                if (img.getHeight() < 64) {
                    BufferedImage skinNew = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = skinNew.createGraphics();
                    g.drawImage(img, 0, 0, null);
                    g.dispose();
                    img = skinNew;
                }

                final BufferedImage finalImg = img;
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    try {
                        ResourceLocation loc = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(
                            "fancymenu_skin_" + Math.abs(targetUrl.hashCode()),
                            new DynamicTexture(finalImg)
                        );
                        Object e = getFieldValue(this, "entity");
                        if (e != null) {
                            setFieldValue(e, "skinLocation", loc);
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                });
            }
        }, "GCT-FancyMenu-SkinAsyncLoader");

        thread.setDaemon(true);
        thread.start();

        return null;
    }

    /**
     * 拦截构造器中对 capeUrl 的同步请求（ordinal = 1）
     */
    @Redirect(
        method = "<init>(Lde/keksuccino/konkrete/properties/PropertiesSection;)V",
        at = @At(
            value = "INVOKE",
            target = "Lde/keksuccino/konkrete/resources/TextureHandler;getWebResource(Ljava/lang/String;)Lde/keksuccino/konkrete/resources/WebTextureResourceLocation;",
            ordinal = 1
        ),
        remap = false
    )
    private WebTextureResourceLocation gct$asyncLoadCape(String capeUrl) {
        if (capeUrl == null || capeUrl.trim().isEmpty()) {
            return null;
        }

        Object ent = getFieldValue(this, "entity");
        if (ent != null) {
            setFieldValue(ent, "capeChecked", true);
        }

        final String targetUrl = capeUrl;
        Thread thread = new Thread(() -> {
            BufferedImage img = downloadImage(targetUrl);
            if (img != null) {
                final BufferedImage finalImg = img;
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    try {
                        ResourceLocation loc = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(
                            "fancymenu_cape_" + Math.abs(targetUrl.hashCode()),
                            new DynamicTexture(finalImg)
                        );
                        Object e = getFieldValue(this, "entity");
                        if (e != null) {
                            setFieldValue(e, "capeLocation", loc);
                        }
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                });
            }
        }, "GCT-FancyMenu-CapeAsyncLoader");

        thread.setDaemon(true);
        thread.start();

        return null;
    }

    /**
     * 在构造函数结束时执行，兜底基于 playerName 的玩家皮肤与披风异步拉取
     */
    @Inject(
        method = "<init>(Lde/keksuccino/konkrete/properties/PropertiesSection;)V",
        at = @At("RETURN"),
        remap = false
    )
    private void gct$afterInit(CallbackInfo ci) {
        try {
            Object ent = getFieldValue(this, "entity");
            if (ent == null) return;

            Object currentSkin = getFieldValue(ent, "skinLocation");
            if (currentSkin == null) {
                // 立即预置默认皮肤，彻底阻断 MenuPlayerEntity.getSkin() 内部的回调阻塞
                setFieldValue(ent, "skinLocation", DefaultPlayerSkin.getDefaultSkinLegacy());
                setFieldValue(ent, "skinChecked", true);
                setFieldValue(ent, "capeChecked", true);

                String pName = (String) getFieldValue(this, "playerName");
                if (pName != null && !pName.trim().isEmpty()) {
                    gct$asyncLoadPlayerSkin(ent, pName.trim());
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private void gct$asyncLoadPlayerSkin(Object ent, String playerName) {
        ResourceLocation cached = getCachedSkin(playerName);
        if (cached != null) {
            setFieldValue(ent, "skinLocation", cached);
            return;
        }

        Thread thread = new Thread(() -> {
            BufferedImage img = downloadImage("https://minotar.net/skin/" + playerName);
            if (img == null) {
                img = downloadImage("https://crafatar.com/skins/" + playerName);
            }

            if (img != null) {
                if (img.getHeight() < 64) {
                    BufferedImage skinNew = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = skinNew.createGraphics();
                    g.drawImage(img, 0, 0, null);
                    g.dispose();
                    img = skinNew;
                }

                final BufferedImage finalImg = img;
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    try {
                        ResourceLocation loc = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(
                            "fancymenu_skin_" + Math.abs(playerName.hashCode()),
                            new DynamicTexture(finalImg)
                        );
                        setFieldValue(ent, "skinLocation", loc);
                        cacheSkin(playerName, loc);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                });
            }
        }, "GCT-FancyMenu-PlayerNameSkinAsyncLoader");

        thread.setDaemon(true);
        thread.start();
    }

    private static BufferedImage downloadImage(String urlStr) {
        try {
            URL u = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) u.openConnection();
            conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(5000);
            try (InputStream in = conn.getInputStream()) {
                if (in != null) {
                    return ImageIO.read(in);
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static Object getFieldValue(Object target, String fieldName) {
        if (target == null) return null;
        Class<?> clazz = target.getClass();
        while (clazz != null && clazz != Object.class) {
            try {
                Field f = clazz.getDeclaredField(fieldName);
                f.setAccessible(true);
                return f.get(target);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (Throwable t) {
                return null;
            }
        }
        return null;
    }

    private static void setFieldValue(Object target, String fieldName, Object value) {
        if (target == null) return;
        Class<?> clazz = target.getClass();
        while (clazz != null && clazz != Object.class) {
            try {
                Field f = clazz.getDeclaredField(fieldName);
                f.setAccessible(true);
                f.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (Throwable t) {
                return;
            }
        }
    }

    private static ResourceLocation getCachedSkin(String playerName) {
        try {
            Class<?> cacheClass = Class.forName("de.keksuccino.fancymenu.menu.fancy.item.playerentity.PlayerEntityCache");
            Method isCachedMethod = cacheClass.getMethod("isSkinCached", String.class);
            if ((Boolean) isCachedMethod.invoke(null, playerName)) {
                Method getSkinMethod = cacheClass.getMethod("getSkin", String.class);
                return (ResourceLocation) getSkinMethod.invoke(null, playerName);
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static void cacheSkin(String playerName, ResourceLocation loc) {
        try {
            Class<?> cacheClass = Class.forName("de.keksuccino.fancymenu.menu.fancy.item.playerentity.PlayerEntityCache");
            Method cacheMethod = cacheClass.getMethod("cacheSkin", String.class, ResourceLocation.class);
            cacheMethod.invoke(null, playerName, loc);
        } catch (Throwable ignored) {
        }
    }
}
