package com.smd.gctcore.client;

import com.smd.gctcore.Tags;
import com.smd.gctcore.common.events.ImmobilizedHandler;
import net.minecraft.util.MovementInput;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/** Stops local movement prediction without interfering with aiming or item use. */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public final class ImmobilizedInputHandler {

    private ImmobilizedInputHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onInput(InputUpdateEvent event) {
        if (!ImmobilizedHandler.isImmobilized(event.getEntityPlayer())) {
            return;
        }
        MovementInput input = event.getMovementInput();
        input.moveForward = 0.0F;
        input.moveStrafe = 0.0F;
        input.forwardKeyDown = false;
        input.backKeyDown = false;
        input.leftKeyDown = false;
        input.rightKeyDown = false;
        input.jump = false;
        event.getEntityPlayer().setSprinting(false);
    }
}
