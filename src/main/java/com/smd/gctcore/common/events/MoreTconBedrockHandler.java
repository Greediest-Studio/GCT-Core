package com.smd.gctcore.common.events;

import com.existingeevee.moretcon.other.MixinEarlyAccessor;
import com.existingeevee.moretcon.traits.ModTraits;
import com.smd.gctcore.misc.moretcon.BedrockBlockChecker;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MoreTconBedrockHandler {

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!(event.getEntityPlayer() instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = event.getEntityPlayer();
        ItemStack held = player.getHeldItem(EnumHand.MAIN_HAND);

        // Only consider TConstruct tools
        if (!MixinEarlyAccessor.getToolCoreClass().isInstance(held.getItem())) {
            return;
        }

        World world = player.world;
        BlockPos pos = event.getPos();
        IBlockState state = world.getBlockState(pos);

        // Check whitelist or interface
        if (!BedrockBlockChecker.isBedrockLike(state)) {
            return;
        }

        // Check trait and tool broken state
        boolean hasBottomsEnd = ModTraits.bottomsEnd.isToolWithTrait(held) && !MixinEarlyAccessor.isStackBroken(held);
        if (!hasBottomsEnd) {
            // If no BottomsEnd, set break speed to 0
            event.setNewSpeed(0f);
            event.setCanceled(true);
            return;
        }

        // Has BottomsEnd: adjust speed according to soft/hard
        float hardness;
        if (BedrockBlockChecker.isSoftBedrock(state)) {
            hardness = Math.min(20, state.getBlockHardness(world, pos) / 2);
        } else {
            hardness = 50f;
        }

        float digSpeed = event.getOriginalSpeed();
        float newSpeed = digSpeed / hardness / 30f;
        event.setNewSpeed(newSpeed);
    }
}
