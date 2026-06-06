package com.smd.gctcore.common.mixin.konkrete;

import de.keksuccino.konkrete.localization.Locals;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

@Mixin(value = Locals.class, remap = false)
public class MixinLocals {

    /**
     * @author smd
     * @reason 原方法在循环中使用 String 拼接完整文件内容，会产生大量临时对象和大数组分配；
     *         改为边读边写，避免整文件驻留内存，降低 GC 压力, 同时保持原行为。
     */
    @Overwrite(remap = false)
    public static void copyLocalsFileToDir(ResourceLocation file, String language, String saveDirWithoutFilename) {
        File lang = new File(saveDirWithoutFilename + "/" + language + ".local");

        if (lang.exists()) {
            lang.delete();
        }

        try (
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                                Minecraft.getMinecraft()
                                        .getResourceManager()
                                        .getResource(file)
                                        .getInputStream(),
                                StandardCharsets.UTF_8
                        )
                );
                BufferedWriter bw = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(lang, false),
                                StandardCharsets.UTF_8
                        )
                )
        ) {
            String line;

            while ((line = br.readLine()) != null) {
                bw.write(line);
                bw.write('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}