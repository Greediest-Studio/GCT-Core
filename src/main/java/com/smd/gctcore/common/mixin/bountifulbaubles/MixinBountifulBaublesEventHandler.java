package com.smd.gctcore.common.mixin.bountifulbaubles;

import baubles.api.BaublesApi;
import cursedflames.bountifulbaubles.event.EventHandler;
import cursedflames.bountifulbaubles.item.ItemAmuletSinWrath;
import cursedflames.bountifulbaubles.item.ModItems;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EventHandler.class, remap = false)
public abstract class MixinBountifulBaublesEventHandler {

    @Inject(method = "playerTick", at = @At("TAIL"), remap = false)
    private static void arr$removeWrathPendantLeakedDamage(TickEvent.PlayerTickEvent event, CallbackInfo ci) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        EntityPlayer player = event.player;
        if (player == null || player.world.isRemote) {
            return;
        }

        if (player.world.getTotalWorldTime() % 10 != 0) {
            return;
        }

        if (BaublesApi.isBaubleEquipped(player, ModItems.sinPendantWrath) == -1) {
            player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE)
                    .removeModifier(ItemAmuletSinWrath.DAMAGE_UUID);
        }
    }
}