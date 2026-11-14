package com.smd.gctcore.common.mixin.aether;

import com.smd.gctcore.Tags;
import com.gildedgames.the_aether.tile_entities.TileEntityTreasureChest;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TileEntityTreasureChest.class, remap = false)
public abstract class MixinDiamondDungeonLoot {

    @Inject(method = "unlock", at = @At("TAIL"), remap = false, require = 1)
    private void gctcore$setDiamondRewardTable(int kind, CallbackInfo ci) {
        if (kind == 3) {
            ((TileEntityLockableLoot) (Object) this).setLootTable(
                    new ResourceLocation(Tags.MOD_ID, "chests/diamond_dungeon_reward"), 0L);
        }
    }
}
