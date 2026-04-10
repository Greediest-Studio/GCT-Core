package com.smd.gctcore.common.items;

import com.smd.gctcore.common.world.CrimsonTempleData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CrimsonAnchorItem extends Item {

    public CrimsonAnchorItem() {
        setTranslationKey("gctcore.crimson_anchor");
        setRegistryName("crimson_anchor");
        setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (world.isRemote) {
            return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
        }

        int dim = player.dimension;
        if (dim != 41) {
            player.sendMessage(new TextComponentTranslation("message.crimson_anchor.notfound"));
            return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
        }

        CrimsonTempleData data = CrimsonTempleData.get(world);
        if (data == null) {
            player.sendMessage(new TextComponentTranslation("message.crimson_anchor.notfound"));
            return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
        }

        BlockPos playerPos = player.getPosition();
        BlockPos nearest = null;
        double bestSq = Double.MAX_VALUE;
        for (BlockPos pos : data.getTemples()) {
            double distSq = pos.distanceSq(playerPos);
            if (distSq < bestSq) {
                bestSq = distSq;
                nearest = pos;
            }
        }

        if (nearest == null) {
            player.sendMessage(new TextComponentTranslation("message.crimson_anchor.notfound"));
        } else {
            player.sendMessage(new TextComponentTranslation("message.crimson_anchor.found", nearest.getX(), nearest.getY(), nearest.getZ()));
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }
}
