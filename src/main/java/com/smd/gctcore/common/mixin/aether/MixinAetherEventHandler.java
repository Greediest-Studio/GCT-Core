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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mixin(AetherEventHandler.class)
public abstract class MixinAetherEventHandler {

    /**
     * Aether iterates the world's live entity list during the world tick.
     * Other tick handlers may add or remove an entity before that iteration
     * finishes, invalidating ArrayList's iterator.  Iterate a snapshot so the
     * dungeon-key invulnerability pass remains stable for this tick.
     */
    @Redirect(
            method = "onWorldTick",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;iterator()Ljava/util/Iterator;"
            ),
            remap = false,
            require = 1
    )
    private Iterator<?> gctcore$iterateEntitySnapshot(List<?> entities) {
        return new ArrayList<>(entities).iterator();
    }

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

