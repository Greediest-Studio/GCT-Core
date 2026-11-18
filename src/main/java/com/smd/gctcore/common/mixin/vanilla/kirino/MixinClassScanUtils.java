package com.smd.gctcore.common.mixin.vanilla.kirino;

import com.smd.gctcore.gctcore;
import io.github.classgraph.ClassGraph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Pseudo
@Mixin(targets = "com.cleanroommc.kirino.ecs.component.scan.helper.ClassScanUtils", remap = false)
public abstract class MixinClassScanUtils {

    @Redirect(
            method = "scan",
            at = @At(
                    value = "INVOKE",
                    target = "Lio/github/classgraph/ClassGraph;acceptPackages([Ljava/lang/String;)Lio/github/classgraph/ClassGraph;"
            ),
            require = 1
    )
    private static ClassGraph gctcore$scopeKirinoClasspath(ClassGraph instance, String[] packages) {
        ClassGraph configured = instance.acceptPackages(packages);
        List<File> scoped = gctcore$resolvePackageSources(packages);
        if (scoped != null && !scoped.isEmpty()) {
            gctcore.LOGGER.info("[GCTCore-KirinoECS] Scoping ClassGraph scan for {} package(s) to {} classpath source(s).",
                    packages.length, scoped.size());
            configured.overrideClasspath(scoped)
                    .disableNestedJarScanning()
                    .disableModuleScanning();
        }
        return configured;
    }

    @Unique
    private static List<File> gctcore$resolvePackageSources(String[] packages) {
        if (packages == null || packages.length == 0) {
            return null;
        }
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = MixinClassScanUtils.class.getClassLoader();
        }
        if (loader == null) {
            return null;
        }
        Set<File> sources = new LinkedHashSet<>();
        for (String pkg : packages) {
            if (pkg == null || pkg.isEmpty()) {
                return null;
            }
            String resourceDir = pkg.replace('.', '/');
            boolean foundForPkg = false;
            try {
                Enumeration<URL> urls = loader.getResources(resourceDir);
                while (urls.hasMoreElements()) {
                    File root = gctcore$extractClasspathRoot(urls.nextElement(), resourceDir);
                    if (root == null) {
                        return null;
                    }
                    sources.add(root);
                    foundForPkg = true;
                }
            } catch (IOException e) {
                return null;
            }
            if (!foundForPkg) {
                // Built-in Kirino packages reside inside the Cleanroom JAR even if directory entries were stripped.
                if (pkg.startsWith("com.cleanroommc.kirino.")) {
                    File kirinoSource = gctcore$resolveCleanroomJar(loader);
                    if (kirinoSource == null) {
                        return null;
                    }
                    sources.add(kirinoSource);
                } else {
                    return null;
                }
            }
        }
        return new ArrayList<>(sources);
    }

    @Unique
    private static File gctcore$extractClasspathRoot(URL url, String resourceDir) {
        try {
            String protocol = url.getProtocol();
            if ("jar".equals(protocol)) {
                String path = url.getPath();
                int bang = path.indexOf("!/");
                if (bang <= 0) {
                    return null;
                }
                URL jarUrl = new URL(path.substring(0, bang));
                return "file".equals(jarUrl.getProtocol()) ? new File(jarUrl.toURI()) : null;
            }
            if ("file".equals(protocol)) {
                File dir = new File(url.toURI());
                int start = 0;
                int len = resourceDir.length();
                while (start < len) {
                    int slash = resourceDir.indexOf('/', start);
                    dir = dir.getParentFile();
                    if (dir == null) {
                        return null;
                    }
                    start = slash < 0 ? len : slash + 1;
                }
                return dir;
            }
        } catch (IOException | URISyntaxException ignored) {
            return null;
        }
        return null;
    }

    @Unique
    private static File gctcore$resolveCleanroomJar(ClassLoader loader) {
        try {
            Class<?> target = Class.forName("com.cleanroommc.kirino.ecs.component.scan.helper.ClassScanUtils", false, loader);
            CodeSource codeSource = target.getProtectionDomain().getCodeSource();
            URL location = codeSource == null ? null : codeSource.getLocation();
            if (location != null && "file".equals(location.getProtocol())) {
                return new File(location.toURI());
            }
        } catch (ClassNotFoundException | URISyntaxException | SecurityException ignored) {
        }
        return null;
    }
}
