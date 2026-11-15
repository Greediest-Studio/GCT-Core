package com.smd.gctcore.common.mixin.vanilla;

import com.smd.gctcore.common.util.AdvancementResourceScope;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;

@Mixin(value = CraftingHelper.class, remap = false)
public abstract class MixinAdvancementArchiveReuse {
    @Redirect(method = {
            "loadFactories(Lnet/minecraftforge/fml/common/ModContainer;Ljava/lang/String;[Lnet/minecraftforge/common/crafting/CraftingHelper$FactoryLoader;)V",
            "findFiles(Lnet/minecraftforge/fml/common/ModContainer;Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/BiFunction;ZZ)Z"
    }, at = @At(value = "INVOKE", target = "Ljava/nio/file/FileSystems;newFileSystem(Ljava/nio/file/Path;Ljava/lang/ClassLoader;)Ljava/nio/file/FileSystem;"), require = 2)
    private static FileSystem gctcore$openArchive(Path path, ClassLoader loader) throws IOException {
        return AdvancementResourceScope.open(path, loader);
    }

    @Redirect(method = {
            "loadFactories(Lnet/minecraftforge/fml/common/ModContainer;Ljava/lang/String;[Lnet/minecraftforge/common/crafting/CraftingHelper$FactoryLoader;)V",
            "findFiles(Lnet/minecraftforge/fml/common/ModContainer;Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/BiFunction;ZZ)Z"
    }, at = @At(value = "INVOKE", target = "Lorg/apache/commons/io/IOUtils;closeQuietly(Ljava/io/Closeable;)V"), require = 2)
    private static void gctcore$closeArchive(Closeable resource) {
        AdvancementResourceScope.close(resource);
    }
}
