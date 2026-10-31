package com.smd.gctcore.common.mixin.fancymenu;

import de.keksuccino.konkrete.resources.WebTextureResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 修复 FancyMenu 在构建主菜单玩家模型时，主线程同步请求网络皮肤导致进程假死（无响应/卡在100%）的问题。
 * 将网络皮肤与披风获取改为后台守护线程异步拉取，并注入连接/读取超时限制。
 */
@Pseudo
@Mixin(targets = "de.keksuccino.fancymenu.menu.fancy.item.playerentity.PlayerEntityCustomizationItem", remap = false)
public abstract class MixinPlayerEntityCustomizationItem {

    @Shadow
    public Object entity;

    @Shadow
    public String playerName;

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

        // 先预置默认史蒂夫/亚历克斯皮肤，防止主线程未就绪前抛空指针，且防止触发 getSkin() 二次兜底
        gct$setEntityField("skinLocation", DefaultPlayerSkin.getDefaultSkinLegacy());

        final String targetUrl = skinUrl;
        Thread thread = new Thread(() -> {
            try {
                URL u = new URL(targetUrl);
                HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(5000);

                BufferedImage img;
                try (InputStream in = conn.getInputStream()) {
                    if (in == null) return;
                    img = ImageIO.read(in);
                }

                if (img != null) {
                    // 兼容旧版 1.7 的 64x32 皮肤转换为 64x64
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
                            gct$setEntityField("skinLocation", loc);
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    });
                }
            } catch (Throwable ignored) {
                // 网络超时或失败时保持默认皮肤，不阻塞任何主线程
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

        final String targetUrl = capeUrl;
        Thread thread = new Thread(() -> {
            try {
                URL u = new URL(targetUrl);
                HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(5000);

                BufferedImage img;
                try (InputStream in = conn.getInputStream()) {
                    if (in == null) return;
                    img = ImageIO.read(in);
                }

                if (img != null) {
                    final BufferedImage finalImg = img;
                    Minecraft.getMinecraft().addScheduledTask(() -> {
                        try {
                            ResourceLocation loc = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(
                                "fancymenu_cape_" + Math.abs(targetUrl.hashCode()),
                                new DynamicTexture(finalImg)
                            );
                            gct$setEntityField("capeLocation", loc);
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    });
                }
            } catch (Throwable ignored) {
            }
        }, "GCT-FancyMenu-CapeAsyncLoader");

        thread.setDaemon(true);
        thread.start();

        return null;
    }

    private void gct$setEntityField(String fieldName, Object value) {
        if (this.entity == null) return;
        try {
            Field f = this.entity.getClass().getField(fieldName);
            f.set(this.entity, value);
        } catch (Throwable t) {
            try {
                Field f = this.entity.getClass().getDeclaredField(fieldName);
                f.setAccessible(true);
                f.set(this.entity, value);
            } catch (Throwable ignored) {
            }
        }
    }
}

