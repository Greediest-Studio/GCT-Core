
package com.gmm.gctold.common.items;

import java.util.List;

import com.gmm.gctold.misc.GctAllCreativeTab;
import com.gmm.gctold.common.events.CreepyWitherDollClick;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;

public class ItemCreepyWitherDoll extends Item {
    public static final Item block = new ItemCreepyWitherDoll();

    public ItemCreepyWitherDoll() {
        this.setMaxDamage(0);
        this.maxStackSize = 1;
        this.setTranslationKey("creepy_wither_doll");
        this.setRegistryName("creepy_wither_doll");
        this.setCreativeTab(GctAllCreativeTab.TAB);

    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        tooltip.add("其实还是蛮可爱的嘛……");
        tooltip.add("在末地原点右键传送至虚空之地");
        tooltip.add("在虚空之地任意地点右键传送回末地");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        CreepyWitherDollClick.run(player, world, (int) player.posX, (int) player.posY, (int) player.posZ);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }
}
