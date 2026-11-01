package com.smd.gctcore.common.mixin.konkrete;

import de.keksuccino.konkrete.resources.WebTextureResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * 为 Konkrete 的 WebTextureResourceLocation 注入连接超时与读取超时，
 * 防止其在 loadTexture 时因无超时设置而无限期阻塞线程。
 */
@Mixin(value = WebTextureResourceLocation.class, remap = false)
public abstract class MixinWebTextureResourceLocation {

    @Redirect(
        method = "loadTexture",
        at = @At(
            value = "INVOKE",
            target = "Ljava/net/URL;openConnection()Ljava/net/URLConnection;"
        ),
        remap = false
    )
    private URLConnection gct$applyTimeout(URL url) throws IOException {
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(5000);
        return conn;
    }
}

