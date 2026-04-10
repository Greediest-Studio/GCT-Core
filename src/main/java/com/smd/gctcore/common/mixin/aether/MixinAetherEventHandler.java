package com.smd.gctcore.common.mixin.aether;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import com.gildedgames.the_aether.AetherEventHandler;

@Mixin(AetherEventHandler.class)
public abstract class MixinAetherEventHandler {

    @Redirect(
            method = "onFillBucket",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/event/entity/player/FillBucketEvent;setResult(Lnet/minecraftforge/fml/common/eventhandler/Event$Result;)V"
            ),
            remap = false
    )
    private void redirectSetResult(FillBucketEvent event, Event.Result result) {
        EntityPlayer player = event.getEntityPlayer();
        ItemStack originalBucket = event.getEmptyBucket();
        ItemStack filledBucket = event.getFilledBucket();

        if (!player.capabilities.isCreativeMode && filledBucket != null) {

            if (originalBucket.getCount() == 1) {
                player.setHeldItem(EnumHand.MAIN_HAND, filledBucket);
            } else {
                originalBucket.shrink(1);
                if (!player.inventory.addItemStackToInventory(filledBucket)) {
                    player.dropItem(filledBucket, false);
                }
            }
        }

        event.setCanceled(true);
    }
}

