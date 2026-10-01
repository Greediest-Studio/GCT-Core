
package com.gmm.gctold.common.items;

import java.util.List;

import com.gmm.gctold.misc.GctAllCreativeTab;
import com.gmm.gctold.common.events.CreepyWitherstormDollClick;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemCreepyWitherstormDoll extends Item {
    public static final Item block = new ItemCreepyWitherstormDoll();

    public ItemCreepyWitherstormDoll() {
        this.setMaxDamage(0);
        this.maxStackSize = 1;
        this.setTranslationKey("creepy_witherstorm_doll");
        this.setRegistryName("creepy_witherstorm_doll");
        this.setCreativeTab(GctAllCreativeTab.TAB);

    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        tooltip.add("也挺可爱的……");
        tooltip.add("它失去了传送的能力，不过可以召唤凋灵风暴！");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        CreepyWitherstormDollClick.run(player, world, (int) player.posX, (int) player.posY, (int) player.posZ);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }
}
