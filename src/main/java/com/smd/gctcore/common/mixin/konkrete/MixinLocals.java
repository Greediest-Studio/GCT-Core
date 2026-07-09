package com.smd.gctcore.common.mixin.konkrete;

import de.keksuccino.konkrete.localization.Locals;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Mixin(value = Locals.class, remap = false)
public class MixinLocals {

    /**
     * @author smd
     * @reason 直接使用 NIO 流拷贝，保持原始文件内容（包括换行符），
     *         代码更简洁，并自动创建缺失的目录。
     */
    @Overwrite(remap = false)
    public static void copyLocalsFileToDir(ResourceLocation file, String language, String saveDirWithoutFilename) {
        try {
            Path lang = Paths.get(saveDirWithoutFilename, language + ".local");

            Files.createDirectories(lang.getParent());

            try (InputStream in = Minecraft.getMinecraft()
                    .getResourceManager()
                    .getResource(file)
                    .getInputStream()) {
                Files.copy(in, lang, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}