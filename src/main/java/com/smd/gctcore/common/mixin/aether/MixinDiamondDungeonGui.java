package com.smd.gctcore.common.mixin.aether;

import com.gildedgames.the_aether.client.gui.GuiTreasureChest;
import com.gildedgames.the_aether.tile_entities.TileEntityTreasureChest;
import com.smd.gctcore.common.integration.aether.AetherDungeonChestKind;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiTreasureChest.class, remap = false)
public abstract class MixinDiamondDungeonGui {

    @Shadow
    private String chestType;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void gctcore$setDiamondChestTitle(InventoryPlayer playerInventory,
                                               TileEntityTreasureChest chest,
                                               CallbackInfo ci) {
        if (chest.getKind() == AetherDungeonChestKind.GCT_DIAMOND_REWARD) {
            this.chestType = I18n.format("gui.gctcore.diamond_reward_chest");
        }
    }
}
