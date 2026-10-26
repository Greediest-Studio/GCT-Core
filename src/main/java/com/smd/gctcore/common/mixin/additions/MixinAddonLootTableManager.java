package com.smd.gctcore.common.mixin.additions;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.tmtravlr.additions.addon.loottables.AddonLootTableManager", remap = false)
public abstract class MixinAddonLootTableManager {

    @Inject(method = "replaceLootTableManager", at = @At("RETURN"), remap = false, require = 1)
    private static void gctcore$syncLootTableManager(MinecraftServer server, CallbackInfo ci) {
        LootTableManager lootTableManager = server.worlds[0].getLootTableManager();

        // 其他维度初始化时复制了旧管理器引用，需同步 Additions 替换后的管理器。
        for (WorldServer world : server.worlds) {
            ObfuscationReflectionHelper.setPrivateValue(
                    World.class, world, lootTableManager, "field_184151_B", "lootTable"
            );
        }
    }
}
